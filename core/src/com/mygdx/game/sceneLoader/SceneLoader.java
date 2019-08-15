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
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.FeatureComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.PickRayComponent;
import com.mygdx.game.features.FeatureAdaptor;
import com.mygdx.game.features.MovingPlatform;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;
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


    private FeatureAdaptor makeFeatureAdaptr(Vector3 position, FeatureAdaptor fa0, Entity target) {

        new MovingPlatform(); // dummy so it is not seen as unreferenced by intelliJ ;)
        FeatureAdaptor adaptor = null;

        if (null != fa0) {

            Class c = fa0.getClass();

            if (c.toString().contains("KillSensor")) {
                Gdx.app.log("asdf", c.toString()); // tmp
            }

            try {
                // The JSON read creates a new instance when sceneData is built, but we want to create a new
                // instance each time to be sure all data is initialized
                // this is only being used for type information ... it is instanced in SceneeData but the idea
                // is for each game Object (Entity) to have it's own feature adatpr instance
                adaptor = (FeatureAdaptor) c.newInstance(); // have to cast this ... can cast to the base-class and it will still take the one of the intended sub-class!!

                if (null != adaptor) {
                    // argument passing convention for model instance is vT, vR, vS (trans, rot., scale) but these can be anything the sub-class wants.
                    // get the "characteristiics" for this type from the JSON
//                        adaptor.vR.set(fa.vR);
                    adaptor.vS.set(fa0.vS);
                    adaptor.vT.set(fa0.vT);

                    adaptor.inverted = fa0.inverted; // I suppose

                    // get location or whatever from object instance data
//                        adaptor.vR0.set(0, 0, 0); // unused ... whatever
//                        adaptor.vS0.set(transform.getScale(tmpV));

                    // grab the starting Origin (translation) of the entity from the instance data
                    adaptor.vT0.set(position);

                    adaptor.init(target);
                }
            } catch (Exception ex) {
                //System.out.println("we're doomed");
                ex.printStackTrace();
            }
        }

        return adaptor;
    }

    public GameFeature getFeature(String featureName){

        SceneData sd = GameWorld.getInstance().getSceneData();
        return sd.features.get(featureName);
    }

//    Entity getPlayer() {
//        GameFeature playerFeature = getFeature("Player");
//        return playerFeature.entity;
//    }

    private void buildModelGroup(Engine engine, String key){

        Gdx.app.log("SceneLoader", "modelGroup = " + key);

        SceneData sd = GameWorld.getInstance().getSceneData();

        ModelGroup mg = sd.modelGroups.get(key);
        if (null == mg) {
            return; // bah
        }

        ModelInfo mi = sd.modelInfo.get(mg.modelName); // mg can't be null ;)
        Model groupModel = null;

        if (null != mi) {

            groupModel = mi.model; // should maybe check model valid ;)

        } else if (null == mg.modelName && 0 == mg.gameObjects.size) {

            return; // bah
        }

        for (GameObject gameObject : mg.gameObjects) {

            if (mg.isKinematic){
                gameObject.isKinematic = mg.isKinematic;
            }
            if (mg.isCharacter){
                gameObject.isCharacter = mg.isCharacter;
            }
            if (null == gameObject.scale) {
                gameObject.scale = new Vector3(1, 1, 1);
            }

            Model model;

            if (null == groupModel){

                String rootNodeId;
                btCollisionShape shape = null;
                ModelInstance instance;

                // look for model Info name matching object name
                ModelInfo mdlInfo = sd.modelInfo.get(gameObject.objectName);

                if (null != mdlInfo) {

                    model = mdlInfo.model;
                    rootNodeId = model.nodes.get(0).id;

                    if (gameObject.mass > 0 && !gameObject.isKinematic) {

                        shape = PrimitivesBuilder.getShape(model.meshes.get(0));
                    }                     // else ... non bullet entity (e.g cars in select screen)

                    if (model.nodes.size > 1) { // multi-node model
                        // "demodularize" model - combine modelParts into single Node for generating the physics shape
                        Model newModel = GfxUtil.modelFromNodes(model); // TODO // model reference for unloading!!!
                        rootNodeId = "node1";
                        instance = ModelInstanceEx.getModelInstance( newModel /* NEW MODEL ! */, rootNodeId, gameObject.scale);
                        shape = PrimitivesBuilder.getShape(newModel.meshes.get(0)); //TODO we would only use this for generating the sHAPE (modelComps to be multi-model-instance)
                    }
                    else {
                        instance = ModelInstanceEx.getModelInstance(model, rootNodeId, gameObject.scale);
                    }
                } else {
                    model = PrimitivesBuilder.getModel();
                    rootNodeId = gameObject.objectName;

                    if (gameObject.isKinematic || gameObject.mass > 0) { // note does not use the gamObject.meshSHape name

                        shape = PrimitivesBuilder.getShape(gameObject.objectName, gameObject.scale); // note: 1 shape re-used
                    }
                    instance = ModelInstanceEx.getModelInstance(model, rootNodeId, gameObject.scale);
                }
                buildGameObject(model, engine, gameObject, instance, shape);
            }
            else
            {
                /* load all nodes from model that match /objectName.*/
                buildNodes(engine, groupModel, gameObject) ;
            }
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
    private void buildNodes(Engine engine, Model model, GameObject gameObject) {
        buildNodes(engine, model, gameObject, null, false) ;
    }

    public void buildNodes(Engine engine, Model model, GameObject gameObject, Vector3 translation, boolean useLocalTranslation) {

        /* load all nodes from model that match /objectName.*/
        for (Node node : model.nodes) {

            String unGlobbedObjectName = gameObject.objectName.replaceAll("\\*$", "");

            if (node.id.contains(unGlobbedObjectName)) {

                // specified node ID means this object is loaded from mondo scene model (where everything should be either static or kinematic )
                ModelInstance mi = ModelInstanceEx.getModelInstance(model, node.id, gameObject.scale);

                if (null != translation) {
                    mi.transform.setTranslation(0, 0, 0); // set trans only (absolute)
                    mi.transform.trn(translation);   // set trans only (offset)
                }

                if (useLocalTranslation){
                    mi.transform.trn(node.localTransform.getTranslation(new Vector3()));
                }

                btCollisionShape shape = null;

                // TODO find another way to get shape - depends on the instance which is bass-ackwards
                // shouldn't need a new shape for each instace - geometery scale etc. belongs to gameObject
                if (null != gameObject.meshShape) {
                    BoundingBox boundingBox = new BoundingBox();
                    Vector3 dimensions = new Vector3();
                    mi.calculateBoundingBox(boundingBox);

                    shape = PrimitivesBuilder.getShape(
                            gameObject.meshShape, boundingBox.getDimensions(dimensions), node); // instance.getNode(node.id),
                }
        /*
        scale is in parent object (not instances) because object should be able to share same bullet shape!
        HOWEVER ... seeing below that bullet comp is made with mesh, we still have duplicated meshes ;... :(
         */
                buildGameObject(model, engine, gameObject, mi, shape);
            } // else  ... bail out if matched an un-globbed name ?
        }
    }

    // gameObject.build() ?      NOTE : copies the passed "instance" ... so caller should discard the reference
    private void buildGameObject(
            Model model, Engine engine, GameObject gameObject, ModelInstance modelInst, btCollisionShape shape) {

        InstanceData id = new InstanceData();
        int n = 0;

        String playerFeatureName = null;
        Entity playerFeatureEntity = null;

        GameFeature playerFeature = getFeature("Player"); // assumes already loaded Characters group ;)

        if (null != playerFeature) {
            playerFeatureName = playerFeature.featureName;
            playerFeatureEntity = playerFeature.getEntity();
        }

        do { // for (InstanceData i : gameObject.instanceData) ... but not, because game objects may have no instance data

            if (gameObject.instanceData.size > 0) {
                id = gameObject.instanceData.get(n++);
            }

            Entity e = buildObjectInstance(modelInst.copy(), gameObject, shape, id);
            engine.addEntity(e);

            ModelComponent mc = e.getComponent(ModelComponent.class);
            mc.model = model;  // bah

            Vector3 position = new Vector3();
            position = mc.modelInst.transform.getTranslation(position);

            FeatureAdaptor adaptor = null;
            if (null != id.adaptr) {
                adaptor = makeFeatureAdaptr(position, id.adaptr, playerFeatureEntity); // needs the origin location ... might as well send in the entire instance transform

                // for now, assign Entity ref to bullet body userValue (only for feature entity right now)
                BulletComponent bc  = e.getComponent(BulletComponent.class);

                if (null != bc){
                    btCollisionObject body = bc.body;
                    if (null != body){
                        // body.setUserValue((int)e);
                        // todo ... need to build a map associating these entities with an int index
                        int next = BulletWorld.getInstance().userToEntityLUT.size;
                        body.setUserValue(next);
                        BulletWorld.getInstance().userToEntityLUT.add(e); // what if e (body) removed?
                    }
                }
            }

            GameFeature gf = getFeature(gameObject.featureName);  // obviously gameObject.feature Name is used as the key

            if (null != gf || null != adaptor) {

                e.add(new FeatureComponent(adaptor));

                if (null != gf) {
                    // stash data in the gameFeature ... this is pretty sketchy as gameFeatures are singular whereas there is possibly multiple GgameObjects/instances that could reference a single GameFeature
                    gf.setEntity(e); ///   bah assigning one entity to a Game Feature

                    if (null == gf.v3data) {
                        gf.v3data = new Vector3(position); // object position from model transform
                    }
                }
            }

            if (null != playerFeatureName) {
                if (gameObject.objectName.equals(playerFeatureName)) {
                    playerFeature.setEntity(e);                        // ok .. only 1 player entity per player Feature
                    e.getComponent(CharacterComponent.class).isPlayer = true;
                }
            }

        } while (null != id && n < gameObject.instanceData.size);
    }

    /* could end up "gameObject.build()" ?? */
    private Entity buildObjectInstance(
            ModelInstance instance, GameObject gameObject, btCollisionShape shape, InstanceData id) {

        Entity e = new Entity();

        if (null != id) {

            if (null != id.rotation) {
                instance.transform.idt();
                instance.transform.rotate(id.rotation);
            }
            if (null != id.translation) {
                instance.transform.setTranslation(0, 0, 0);
                instance.transform.trn(id.translation);
            }
            if (null != id.color) {
                ModelInstanceEx.setColorAttribute(instance, id.color, id.color.a); // kind of a hack ;)
            }
        }

        ModelComponent mc = new ModelComponent(instance);
        mc.isShadowed = gameObject.isShadowed; // disable shadowing of skybox)
        e.add(mc);

        if (null != shape) {
            BulletComponent bc = new BulletComponent(shape, instance.transform, gameObject.mass);
            e.add(bc);

            // special sauce here for static entity
            if (gameObject.isKinematic) {  // if (0 == mass) ??
// set these flags in bullet comp?
                bc.body.setCollisionFlags(
                        bc.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
                bc.body.setActivationState(Collision.DISABLE_DEACTIVATION);
            }
        }

//        if (gameObject.isSteerable) {
        if (gameObject.isCharacter) {
            e.add(new CharacterComponent());
        }

        if (gameObject.isPickable) {
            e.add(new PickRayComponent());
        }

        return e;
    }

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
                cpObject.instanceData.add(i);

                mg.gameObjects.add(cpObject);
            }

            cpGameData.modelGroups.put(key /* sd.modelGroups.get(key).groupName */, mg);
        }
      /*
        saveData(cpGameData); // this is to capture new Classes at runtime (e.g. need help getting the format of new Class being added to the SceneData)
      */
    }
}
