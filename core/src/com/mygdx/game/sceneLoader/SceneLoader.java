/*
 * Copyright (c) 2021 Glenn Neidermeier
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
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
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
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.GameWorld;

import java.util.HashMap;

import static com.badlogic.gdx.graphics.GL20.GL_FRONT;

/**
 * Created by neiderm on 12/18/17.
 */

public class SceneLoader implements Disposable {

    private static final String CLASS_STRING = "SceneLoader";

    // Model Group names
    private static final String STATIC_OBJECTS = "InstancedModelMeshes";
    private static final String USER_MODEL_PARTS = "UserModelPartsNodes";
    private static final String USER_MODEL_INFO = "UserMeshesModel";
    private static final String LOCAL_PLAYER_MGRP = "LocalPlayer";
    private static final String SFX_GAME = "sfxGame";
    private static AssetManager assets; // see comment below on instantiating it
    private static Model userModel;

    public static class SoundInfo {
        public Sound sfx;
        SoundInfo(Sound sfx) {
            this.sfx = sfx;
        }
    }

    private static final HashMap<String, SoundInfo> sfxGameCache = new HashMap<>();


    public SceneLoader(SceneData sd) {
        /*
         * Assigning a value to a static field in a constructor could cause unreliable behavior at
         * runtime since it will change the value for all instances of the class.
         * ... or so they say ;)
         */
        if (null != sd) {
            assets = new AssetManager();

            for (String key : sd.modelInfo.keySet()) {

                String fn = sd.modelInfo.get(key).fileName;

                if (null != fn) {
                    if (fn.contains(".png") || fn.contains(".jpg")) {
                        assets.load(fn, Texture.class);
                    } else if (fn.contains(".g3d")) {
                        assets.load(fn, Model.class);
                    } else if (fn.contains(".ogg")) {
                        assets.load(fn, Music.class);
                    }
                }
            }
            /*
             * special sauce to organize and retrieve audio files
             */
            HashMap<String, ModelGroup> mgrps = sd.modelGroups;

            ModelGroup sfGameGroup = mgrps.get(SFX_GAME);

            final String ff = "sfx/";

            for (GameObject gg : sfGameGroup.elements) {
                String fn = ff + gg.objectName;

                if (fn.contains(".ogg")) {
                    assets.load(fn, Sound.class);
                }
            }
        }
    }

    public SoundInfo getSoundInfo(String key) {
        return sfxGameCache.get(key);
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
                } else if (fn.contains(".png")) {
                    // loads texture image files below with user model
                    // sd.modelInfo.get(key).model = assets.get(sd.modelInfo.get(key).fileName, Texture.class);
                }
            }
        }

        ModelGroup sfxGame = sd.modelGroups.get(SFX_GAME);

        for (GameObject gg : sfxGame.elements) {

            String ff = "sfx/" + gg.objectName;
            Sound theSound = assets.get(ff, Sound.class);

            String key = gg.featureName.substring(0, 3);
            sfxGameCache.put(key, new SoundInfo(theSound));
        }

        /*
         * simple parts model bult up from instances created by Model Builder .part() ... only need
         * built on Screen Loading (should not be disposed on screen restart)
         */
        ModelGroup umg = sd.modelGroups.get(USER_MODEL_PARTS);

        if (null != umg) { // may or may not be defined in scene data
            userModel = makeUserModel(umg); // stores reference to model in the dummy ModelInfo block
            /*
             * use the dummy ModelInfo block to store reference to the newly-constructed model
             */
            ModelInfo textureModelInfo = sd.modelInfo.get(USER_MODEL_INFO);

            if (null != textureModelInfo) {
                textureModelInfo.model = userModel;
            }
            // please ... release me .. let me go!  this Model Group no longer needed, if only i could purge!
        }
        /*
         * create the player Model group using the special Game Feature defined by the loader, or get
         * localplayer model group if one is defined in json (which also needs to know which object name
         * to load).
         */
        GameFeature playerFeature = GameWorld.getInstance().getFeature(GameWorld.LOCAL_PLAYER_FNAME);

        if (null != playerFeature) {

            ModelGroup tmg = sd.modelGroups.get(LOCAL_PLAYER_MGRP);

            if (null != tmg) {  // Select screen does not define a player group
                GameObject gameObject = tmg.getElement(0);
                gameObject.mass = 5.1f;   // should be from the model or something
                gameObject.isPlayer = true;
                gameObject.objectName = playerFeature.getObjectName();
            }
        }
    }

    private static void buildModelGroup(Engine engine, String key) {

        Gdx.app.log(CLASS_STRING, "modelGroup = " + key);

        SceneData sd = GameWorld.getInstance().getSceneData();
        ModelGroup mg = sd.modelGroups.get(key);

        if (null != mg) {
            mg.build(engine);
        }
    }

    /*
     * keeps count of number of crapiums in each areener
     */
    private static int numberOfCrapiums;

    public static int getNumberOfCrapiums() {
        return numberOfCrapiums;
    }

    public static void incNumberOfCrapiums() {
        numberOfCrapiums += 1;
    }

    public static void buildScene(Engine engine) {

        numberOfCrapiums = 0;

        SceneData sd = GameWorld.getInstance().getSceneData();
        if (null != sd) {
            // build the model groups
            for (String key : sd.modelGroups.keySet()) {
                Gdx.app.log(CLASS_STRING, key);
                // skip this model group it is built from scratch during asset loading (not loaded from g3db)
                if (key.equals(USER_MODEL_PARTS)) {
                    continue; // how to remove Model Group ?
                }
                if (key.equals(LOCAL_PLAYER_MGRP)) { // mt
                }
                if (key.equals(STATIC_OBJECTS)) { // mt
                }
                buildModelGroup(engine, key);
            }
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

        int objectCountFlag = 0;
        final int SKY_BOX_OBJECT = 0;

// Instead of being a node loaded from a mesh model, the element represents a texture file 
// to be loaded, and the node mesh generated by matching the string (case-insensitive) e.g. 
// sphere, box etc with the object name to select the appropriate mb.part
        for (GameObject go : mg.elements) {

            ModelInfo mi = sd.modelInfo.get(go.objectName);

            Texture tex = null;

            if (null != mi && null != mi.fileName) {
                tex = assets.get(mi.fileName, Texture.class);
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

//            if  isReverseFace ........... use item 0 for skybox - no other item needs reverse face right now
            if (SKY_BOX_OBJECT == objectCountFlag++) {
                mat.set(IntAttribute.createCullFace(GL_FRONT)); // set reverse face culling for skybox
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

        return mb.end();
    }

    @Override
    public void dispose() {

        if (null != assets) {
            assets.dispose();
        }
        // dispose and set the static variable to null
        if (null != userModel) {
            userModel.dispose();
        }
    }
}
