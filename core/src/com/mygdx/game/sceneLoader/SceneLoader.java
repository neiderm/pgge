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
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.FeatureComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.features.FeatureAdaptor;
import com.mygdx.game.util.PrimitivesBuilder;

import java.util.Random;

import static com.badlogic.gdx.graphics.GL20.GL_FRONT;


/**
 * Created by neiderm on 12/18/17.
 */

public class SceneLoader implements Disposable {

    private static boolean useTestObjects = true;
    private static AssetManager assets;
    private static Model texModel;

    public SceneLoader() {

//        gameData = new SceneData();
/*
Get rid of this? .. originally, to seed the first gaem data file .. no longer current or useful.
Once the creation of those objects from JSON was understodd and done conrrectly , there was never
again a need to creat3e these directly in code
        ModelGroup tanksGroup = new ModelGroup("tanks");
        tanksGroup.gameObjects.add(new GameData.GameObject("ship", "mesh Shape"));
        tanksGroup.gameObjects.add(new GameData.GameObject("tank", "mesh Shape"));
        gameData.modelGroups.put("tanks", tanksGroup);

        ModelGroup sceneGroup = new ModelGroup("scene", "scene");
        sceneGroup.gameObjects.add(new GameData.GameObject("Cube", "none"));
        sceneGroup.gameObjects.add(new GameData.GameObject("Platform001", "convexHullShape"));
        sceneGroup.gameObjects.add(new GameData.GameObject("Plane", "triangleMeshShape"));
        sceneGroup.gameObjects.add(new GameData.GameObject("space", "none"));
        gameData.modelGroups.put("scene", sceneGroup);

        ModelGroup objectsGroup = new ModelGroup("objects", "objects");
        objectsGroup.gameObjects.add(new GameData.GameObject("Crate*", "btBoxShape")); // could be convexHull? (gaps?)
        gameData.modelGroups.put("objects", objectsGroup);

        ModelGroup primitivesGroup = new ModelGroup("primitives", "primitivesModel");
        GameData.GameObject object = new GameData.GameObject("boxTex", "btBoxShape"); // could be convexHull? (gaps?)
        object.instanceData.add( new GameData.GameObject.InstanceData(new Vector3(0, 4, -15), new Vector3(0, 0, 0)));
        object.instanceData.add( new GameData.GameObject.InstanceData(new Vector3(-2, 4, -15), new Vector3(0, 0, 0)));
        object.instanceData.add( new GameData.GameObject.InstanceData(new Vector3(-4, 4, -15), new Vector3(0, 0, 0)));
        object.instanceData.add( new GameData.GameObject.InstanceData(new Vector3(0, 6, -15), new Vector3(0, 0, 0)));
        object.instanceData.add( new GameData.GameObject.InstanceData(new Vector3(-2, 6, -15), new Vector3(0, 0, 0)));
        object.instanceData.add( new GameData.GameObject.InstanceData(new Vector3(-4, 6, -15), new Vector3(0, 0, 0)));
        primitivesGroup.gameObjects.add(object);
        gameData.modelGroups.put("primitives", primitivesGroup);

        gameData.modelInfo.put("scene", new ModelInfo("scene", "data/scene.g3dj"));
        gameData.modelInfo.put("landscape", new ModelInfo("landscape", "data/landscape.g3db"));
        gameData.modelInfo.put("ship", new ModelInfo("ship", "tanks/ship.g3db"));
        gameData.modelInfo.put("tank", new ModelInfo("tank", "tanks/panzerwagen.g3db"));
        gameData.modelInfo.put("objects", new ModelInfo("objects", "data/cubetest.g3dj"));
        gameData.modelInfo.put("primitives", new ModelInfo("primitivesModel", null));
*/
//        saveData(); // tmp: saving to temp file, don't overwrite what we have

//        initializeGameData();

        SceneData sd = GameWorld.getInstance().getSceneData();

        assets = new AssetManager();
/*
        assets.load("data/cubetest.g3dj", Model.class);
        assets.load("data/landscape.g3db", Model.class);
        assets. load("tanks/ship.g3db", Model.class);
        assets.load("tanks/panzerwagen.g3db", Model.class);
        assets.load("data/scene.g3dj", Model.class);
*/
//        int i = gameData.modelInfo.values().size();
        for (String key : sd.modelInfo.keySet()) {
            String fn = sd.modelInfo.get(key).fileName;
            if (null != sd.modelInfo.get(key).fileName) {
                if (sd.modelInfo.get(key).fileName.contains(".png")){

                    assets.load(sd.modelInfo.get(key).fileName, Texture.class);
                }
                else                 if (sd.modelInfo.get(key).fileName.contains(".g3d")){

                    assets.load(sd.modelInfo.get(key).fileName, Model.class);
                }
//                assets.load(sd.modelInfo.get(key).fileName, Model.class);
            }
        }
///*
        SceneData.saveData(sd); // write it out as read for comparison to tthe Scene Data loaded from JSON (I like to keep the same order as the JSON formatter writes it )
//*/
    }

    public static AssetManager getAssets() {
        return assets;
    }



    /*
        http://niklasnson.com/programming/network/tips%20and%20tricks/2017/09/15/libgdx-save-and-load-game-data.html
    */

/*    public void initializeGameData() {
        if (!fileHandle.exists()) {
            gameData = new GameData();

            saveData();
        } else {
            loadData();
        }
    }*/


    /*
     * build up the scene chunk after the background asset loading process is finished
     */
    public static void doneLoading() {

        SceneData sd = GameWorld.getInstance().getSceneData();

        /* get references to the loaded models */
        for (String key : sd.modelInfo.keySet()) {

            if (null != sd.modelInfo.get(key).fileName) {

                if (sd.modelInfo.get(key).fileName.contains(".png")){

//                    sd.modelInfo.get(key).model = assets.get(sd.modelInfo.get(key).fileName, Texture.class);
                    Gdx.app.log("scene loaer", "load a texture");
                }
                else
                if (sd.modelInfo.get(key).fileName.contains(".g3d")){

                    sd.modelInfo.get(key).model = assets.get(sd.modelInfo.get(key).fileName, Model.class);
                }
            }
        }

        /* next step is for the client SCreen to build up the scene chunk */

        String pn = SceneData.getPlayerObjectName();

        if (null != pn) {
            GameFeature gf = new GameFeature(pn);
            sd.features.put("Player", gf);
        }


        /*
         * Textureds Model only need built on Screen Loading (should not be necessary  on a screen Re-Start)
         */
        ModelGroup tnmg = sd.modelGroups.get("TexturesNodes");

        if (null != tnmg) { // may or may not be define in scene data

            if (null != texModel){
                Gdx.app.log("SceneLoader", "tex Model not been disposed properly?");
            }
            texModel = makeTexModel(tnmg); // stores reference to model in the dummy ModelInfo block

            /*
             * use the dummy ModelInfo block to store reference to the newly-constructred model
             */
            ModelInfo textureModelInfo = sd.modelInfo.get("TexturesMdlInfo");

            if (null != textureModelInfo) {

                textureModelInfo.model = texModel;
            }
        }

        Gdx.app.log("SceneLoader", "INitializeation complete!");
    }

    private static void createTestObjects(Engine engine) {

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

    public static void buildScene(Engine engine) {

        createTestObjects(engine); // tmp

        // if there is a Characters group, try to get a Player
        buildModelGroup(engine, "Characters"); // possible to eliminate "isCharacter" ?

        SceneData sd = GameWorld.getInstance().getSceneData();

        for (String key : sd.modelGroups.keySet()) {

            if (key.equals("Characters")) {
                continue;
            }
            if (key.equals("TexturesModel")) {
                continue;
            }
            if (key.equals("TexturesNodes")) {
                continue; // removed Model Group (shouldn't be hre)
            }
            /*
             * Hacking this in here for now I guess (in ModelGroup? idfk
             *  texture objects  needs special sauce so is setup in own modelGroup
             */
            if (key.equals("TextureObjects")) {

                ModelGroup mg = sd.modelGroups.get(key);

                TEMP_mkSkySphere(engine, mg.getGameObject(0)); // tmp ... 0

                continue;
            }

            buildModelGroup(engine, key);
        }

        // any other one-time setups after all file data object loaded ... features set target to player by default
        GameFeature playerFeature = GameWorld.getInstance().getFeature("Player");

        if (null != playerFeature) {

            ImmutableArray<Entity> feats = engine.getEntitiesFor(Family.all(FeatureComponent.class).get());

            for (Entity ee : feats) {

                FeatureAdaptor fa = ee.getComponent(FeatureComponent.class).featureAdpt;

                if (null != fa) { // have to set default ... default target is player ...

                    fa.init(playerFeature.getEntity());
                }
            }
        }
    }

    private static void TEMP_mkSkySphere(Engine engine, GameObject go) {

        SceneData sd = GameWorld.getInstance().getSceneData();

        String modelName = go.objectName;    // use "objectName" to look up file name in ModelInfo section
        ModelInfo mi = sd.modelInfo.get(modelName);

        Vector3 size = new Vector3(1, 1, 1);

        if (null != go.scale) {
            size.set(go.scale);
        }

        Vector3 trans = null; // defaults to 0 origin for now

        /* nothing special about the skySphere .. ordinary unit sphere with a material (color and face attributes but no texture) */
        Entity e = PrimitivesBuilder.load(
                PrimitivesBuilder.getModel(),
                go.objectName /*"skySphere"*/, null, new Vector3(size.x, size.x, size.x), 0, trans);
        engine.addEntity(e);

        if (null != mi) {
//                    FileHandle fh = Gdx.files.internal(mi.fileName);
//                    if (null != fh)
            Texture tex = new Texture(Gdx.files.internal(mi.fileName /*"data/redsky.png"*/), true);

            ModelInstance inst = e.getComponent(ModelComponent.class).modelInst;
            Material mat = inst.materials.get(0);
////        if (null != mat)
            mat.clear(); // clear out ColorAttribute of whatever if there was default material?  i guess this ok to do
            mat.set(TextureAttribute.createDiffuse(tex));

            mat.set(IntAttribute.createCullFace(GL_FRONT)); // haven't codified this attribute yet
        }
    }

    private static Model makeTexModel(ModelGroup mg) {

        SceneData sd = GameWorld.getInstance().getSceneData();

        final ModelBuilder mb = new ModelBuilder();
        long attributes =
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        attributes |= VertexAttributes.Usage.TextureCoordinates;

        mb.begin();

        for (GameObject go : mg.gameObjects) {

            ModelInfo mi = sd.modelInfo.get(go.objectName);
            String fileName = mi.fileName;

            Material mat = new Material();
            Texture tex = assets.get(fileName, Texture.class);

            if (null != tex) {
                mat.set(TextureAttribute.createDiffuse(tex));
            } else {
                mat.set(ColorAttribute.createDiffuse(Color.MAROON));// shouldn't be here?
            }

            if (go.iSWhatever) { // isReverseFace
                mat.set(IntAttribute.createCullFace(GL_FRONT)); // will reuse this Game oBject field
            }

            mb.node().id = go.objectName;

            if (go.objectName.contains("box")) {

                mb.part("box", GL20.GL_TRIANGLES, attributes, mat).box(1f, 1f, 1f);

            } else
                if (
                        go.objectName.contains("Sphere") // ... case hmmmm we'lll see
                    || go.objectName.contains("sphere")) {

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
        if (null != texModel){
            texModel.dispose();
            texModel = null;
        }

// new test file writer
        SceneData cpGameData = new SceneData();
        SceneData sd = GameWorld.getInstance().getSceneData();

        for (String key : sd.features.keySet()) {

            GameFeature gf = new GameFeature(/*key*/);
//            gf.featureAdaptor = new OmniSensor();
//            gf.featureAdaptor.vR = new Vector3(1, 2, 3);

            cpGameData.features.put(key, gf);
        }

        for (String key : sd.modelGroups.keySet()) {

            ModelGroup mg = new ModelGroup(key /* sd.modelGroups.get(key).groupName */);

//            for (GameObject o : sd.modelGroups.get(key).gameObjects) {
//
//                GameObject cpObject = new GameObject(o.objectName, o.meshShape);
//
//                InstanceData i = new InstanceData();
//                i.adaptr = new MovingPlatform();
//                i.adaptr.vT = new Vector3(0.4f, 0.5f, 0.6f);
//                cpObject.getInstanceData().add(i);
//
//                mg.gameObjects.add(cpObject);
//            }
//
//            cpGameData.modelGroups.put(key /* sd.modelGroups.get(key).groupName */, mg);
        }
      /*
        saveData(cpGameData); // this is to capture new Classes at runtime (e.g. need help getting the format of new Class being added to the SceneData)
      */
    }
}
