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
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.GameWorld;
import com.mygdx.game.features.MovingPlatform;
import com.mygdx.game.util.PrimitivesBuilder;

import java.util.Random;


/**
 * Created by neiderm on 12/18/17.
 */

public class SceneLoader implements Disposable {

    private static boolean useTestObjects = true;
    private AssetManager assets;


    public SceneLoader(){

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
            if (null != sd.modelInfo.get(key).fileName) {
                assets.load(sd.modelInfo.get(key).fileName, Model.class);
            }
        }
///*
        SceneData.saveData(sd); // write it out as read for comparison to tthe Scene Data loaded from JSON (I like to keep the same order as the JSON formatter writes it )
//*/
    }

    public AssetManager getAssets() {
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
    public void doneLoading() {

        SceneData sd = GameWorld.getInstance().getSceneData();

        /* get references to the loaded models */
        for (String key : sd.modelInfo.keySet()) {
            if (null != sd.modelInfo.get(key).fileName) {
                sd.modelInfo.get(key).model = assets.get(sd.modelInfo.get(key).fileName, Model.class);
            }
        }

        /* next step is for the client SCreen to build up the scene chunk */

        String pn = SceneData.getPlayerObjectName();

        if (null != pn){
            GameFeature gf = new GameFeature(pn);
            sd.features.put("Player", gf);
        }
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

//    Entity getPlayer() {
//        GameFeature playerFeature = getFeature("Player");
//        return playerFeature.entity;
//    }

    private void buildModelGroup(Engine engine, String key){

        Gdx.app.log("SceneLoader", "modelGroup = " + key);

        SceneData sd = GameWorld.getInstance().getSceneData();
        ModelGroup mg = sd.modelGroups.get(key);

        if (null != mg) {

            mg.build(engine);
        }
    }

    public void buildScene(Engine engine) {

        createTestObjects(engine); // tmp

        // if there is a Characters group, try to get a Player
        buildModelGroup(engine, "characters");

        SceneData sd = GameWorld.getInstance().getSceneData();

        for (String key : sd.modelGroups.keySet()) {

            if (key.equals("characters")){
                continue;
            }

            buildModelGroup(engine, key);
        }

//        GameFeature playerFeature = getFeature("Player");
//
//        // any other one-time setups after all file data object loaded ...
//        // Select all entities from Engine with FeatureComp, set target to player
//        ImmutableArray<Entity> feats = engine.getEntitiesFor(Family.all(FeatureComponent.class).get());
//
//        for (Entity fe : feats){
//            FeatureAdaptor fa = fe.getComponent(FeatureComponent.class).featureAdpt;
//
//            if (null != fa){ // have to set default ... default target is player ...
//                // only set the target if it is not already set
//                if (null == fa.getTarget() && null != playerFeature) {
//                    fa.setTarget(playerFeature.getEntity());
//                }
//            }
//        }
    }

    /*
     * searching the group model for the given gameObject.objectName* ...
     * may not be super efficient and  ... increasing number of model nodes ???
     * However walking the model is needed for globbed object name, not
     * seeing a more efficient way right now.
     */

    @Override
    public void dispose() {

        // The Model owns the meshes and textures, to dispose of these, the Model has to be disposed. Therefor, the Model must outlive all its ModelInstances
        //  Disposing the file will automatically make all instances invalid!
        assets.dispose();

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

            for (GameObject o : sd.modelGroups.get(key).gameObjects) {

                GameObject cpObject = new GameObject(o.objectName, o.meshShape);

                InstanceData i = new InstanceData();
                i.adaptr = new MovingPlatform();
                i.adaptr.vT = new Vector3(0.4f, 0.5f, 0.6f);
                cpObject.getInstanceData().add(i);

                mg.gameObjects.add(cpObject);
            }

            cpGameData.modelGroups.put(key /* sd.modelGroups.get(key).groupName */, mg);
        }
      /*
        saveData(cpGameData); // this is to capture new Classes at runtime (e.g. need help getting the format of new Class being added to the SceneData)
      */
    }
}
