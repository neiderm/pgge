/*
 * Copyright (c) 2019 Glenn Neidermeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mygdx.game.sceneLoader;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.GameWorld;
import com.mygdx.game.util.PrimitivesBuilder;

import java.util.Random;

import static com.badlogic.gdx.graphics.GL20.GL_FRONT;


/**
 * Created by neiderm on 12/18/17.
 */

public class SceneLoader implements Disposable {

    // Model Group name,  has to be fixed
    private static final String USER_MODEL_PARTS = "UserModelPartsNodes";
    private static final String LOCAL_PLAYER_MGRP = "LocalPlayer";

    private static boolean useTestObjects = true;
    private static AssetManager assets;
    private static Model userModel;

    public SceneLoader() {

        SceneData sd = GameWorld.getInstance().getSceneData();

        assets = new AssetManager();

        for (String key : sd.modelInfo.keySet()) {

            String fn = sd.modelInfo.get(key).fileName;

            if (null != fn) {
                if (fn.contains(".png")) {
                    assets.load(fn, Texture.class);
                } else if (fn.contains(".g3d")) {
                    assets.load(fn, Model.class);
                }
            }
        }
    }

    public static AssetManager getAssets() {
        return assets;
    }

    /*
     * build up the scene chunk after the background asset loading process is finished
     */
    public static void doneLoading() {

        SceneData sd = GameWorld.getInstance().getSceneData();

        /* get references to the loaded models */
        for (String key : sd.modelInfo.keySet()) {

            String fn = sd.modelInfo.get(key).fileName;

            if (null != fn) {
                if (fn.contains(".g3d")) {

                    sd.modelInfo.get(key).model = assets.get(fn, Model.class);
                }
//                else if (fn.contains(".png")) {
//// loads texture image files below with user model
////                    sd.modelInfo.get(key).model = assets.get(sd.modelInfo.get(key).fileName, Texture.class);
//                }
            }
        }

        /*
         * simple parts Model bult up from instances created by Model Builder .part() ... only need
         * built on Screen Loading   (should not be disposed on a screen Re-Start)
         */
        ModelGroup umg = sd.modelGroups.get(USER_MODEL_PARTS);

        if (null != umg) { // may or may not be define in scene data

            if (null != userModel) {
                Gdx.app.log("SceneLoader", "tex Model not been disposed properly?");
            }
            userModel = makeUserModel(umg); // stores reference to model in the dummy ModelInfo block

            /*
             * use the dummy ModelInfo block to store reference to the newly-constructred model
             */
            ModelInfo textureModelInfo = sd.modelInfo.get("UserMeshesModel");

            if (null != textureModelInfo) {

                textureModelInfo.model = userModel;
            }
            // please ... release me .. let me go!  this Model Group no longer needed, if only i could purge!
        }

        /*
         * create the player Model group using the special Game Feature defined by the loader, or get
         * localplayer model group if one is defined in json (which also needs to know which object name
         * to load).
         * is Player flag is hack to tell object builder to grab the entity of the player and store it in the
         * global player feature.
         */
        GameFeature playerFeature =
                GameWorld.getInstance().getFeature(SceneData.LOCAL_PLAYER_FNAME);

        String localPlayerObjectname = null;

        if (null != playerFeature) {
            localPlayerObjectname = playerFeature.getObjectName();

            ModelGroup tmg = sd.modelGroups.get(LOCAL_PLAYER_MGRP);

            if (null != tmg) {  // select screen doesnt define a player group

                GameObject gameObject = tmg.getElement(0); // snhould be only 1!

                if (null == gameObject) {   // new instance of model gruop, game object
                    gameObject = new GameObject(localPlayerObjectname);
                    tmg.addElement(gameObject);
                }

                gameObject.mass = 5.1f;   // should be from the model or something
                gameObject.isPlayer = true; ////////////////// bah look at me hack
                gameObject.objectName = localPlayerObjectname;
            }
        }
    }

    public static void _createTestObjects(Engine engine) {

        Random rnd = new Random();

        int N_ENTITIES = 10;
        final int N_BOXES = 4;
        if (!useTestObjects) N_ENTITIES = 0;
        Vector3 size = new Vector3();

        for (int i = 0; i < N_ENTITIES; i++) {

            size.set(rnd.nextFloat() + .1f, rnd.nextFloat() + .1f, rnd.nextFloat() + .1f);
            size.scl(2.0f); // this keeps object "same" size relative to previous primitivesModel size was 2x

            Vector3 translation =
                    new Vector3(rnd.nextFloat() * 10.0f - 5f, rnd.nextFloat() + 25f, rnd.nextFloat() * 10.0f - 5f);

            if (i < N_BOXES) {
                btCollisionShape shape = PrimitivesBuilder.getShape("boxTex", size); // note: 1 shape re-used
                engine.addEntity(
                        PrimitivesBuilder.load(PrimitivesBuilder.getModel(), "boxTex", shape, size, size.x, translation));

            } else {
                btCollisionShape shape = PrimitivesBuilder.getShape("sphereTex", size); // note: 1 shape re-used
                engine.addEntity(
                        PrimitivesBuilder.load(PrimitivesBuilder.getModel(), "sphereTex", shape, new Vector3(size.x, size.x, size.x), size.x, translation));

            }
        }
    }

    private static void buildModelGroup(Engine engine, String key) {

        Gdx.app.log("SceneLoader", "modelGroup = " + key);

        SceneData sd = GameWorld.getInstance().getSceneData();
        ModelGroup mg = sd.modelGroups.get(key);

        if (null != mg) {

            mg.build(engine);
        }
    }

    public static int numberOfCrapiums;

    public static void buildScene(Engine engine) {

        numberOfCrapiums = 0;


        SceneData sd = GameWorld.getInstance().getSceneData();

        /*
         * build  the model groups
         */
        for (String key : sd.modelGroups.keySet()) {

            if (key.equals(USER_MODEL_PARTS)) {
                continue; // how to remove Model Group ?
            }

            buildModelGroup(engine, key);
        }
    }

    /*
     * User Model built from simple mesh parts and material, optionally texture or just color. This
     * is tied to Asset Manager for want of texture files to be managed centrally as much as practicable.
     */
    private static Model makeUserModel(ModelGroup mg) {

        SceneData sd = GameWorld.getInstance().getSceneData();

        final ModelBuilder mb = new ModelBuilder();

        mb.begin();

        for (GameObject go : mg.elements) {

            ModelInfo mi = sd.modelInfo.get(go.objectName);

            Texture tex = null;

            if (null != mi) {
                if (null != mi.fileName) {
                    tex = assets.get(mi.fileName, Texture.class);
                }
            }

            long attributes =
                    VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
            attributes |= VertexAttributes.Usage.TextureCoordinates;

            Material mat = new Material();

            if (null != tex) {
                mat.set(TextureAttribute.createDiffuse(tex));
            } else {
                mat.set(ColorAttribute.createDiffuse(Color.CYAN));// shouldn't be here?
            }

            if (go.iSWhatever) { // isReverseFace
                mat.set(IntAttribute.createCullFace(GL_FRONT)); // will reuse this Game oBject field
            }

            mb.node().id = go.objectName;

            String compString = go.objectName.toLowerCase();

            /*
             * pretty dumb scheme here
             */
            if (compString.contains("box")) {

                mb.part("box", GL20.GL_TRIANGLES, attributes, mat).box(1f, 1f, 1f);

            } else if (compString.contains("sphere")) {

                mb.part("sphere", GL20.GL_TRIANGLES, attributes, mat).sphere(1f, 1f, 1f, 10, 10);
            }
        }

        Model model = mb.end();

        return model;
    }


    @Override
    public void dispose() {

        // The Model owns the meshes and textures, to dispose of these, the Model has to be disposed. Therefor, the Model must outlive all its ModelInstances
        //  Disposing the file will automatically make all instances invalid!
        assets.dispose();

        /* be careful this one isn't constructed unless defined in json */
        if (null != userModel) {
            userModel.dispose();
            userModel = null;
        }
    }
}
