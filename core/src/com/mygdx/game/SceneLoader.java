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

package com.mygdx.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.PickRayComponent;
import com.mygdx.game.screens.SceneData;
import com.mygdx.game.util.MeshHelper;
import com.mygdx.game.util.ModelInstanceEx;
import com.mygdx.game.util.PrimitivesBuilder;

import java.util.Random;


/**
 * Created by neiderm on 12/18/17.
 */

public class SceneLoader implements Disposable {

    private SceneData gameData;
    private static boolean useTestObjects = true;
    private AssetManager assets;

//    private static final float DEFAULT_TANK_MASS = 5.1f; // idkf


    public SceneLoader(String path) {

        gameData = new SceneData();
/*
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

        gameData = SceneData.loadData(path);

        assets = new AssetManager();
/*
        assets.load("data/cubetest.g3dj", Model.class);
        assets.load("data/landscape.g3db", Model.class);
        assets. load("tanks/ship.g3db", Model.class);
        assets.load("tanks/panzerwagen.g3db", Model.class);
        assets.load("data/scene.g3dj", Model.class);
*/
//        int i = gameData.modelInfo.values().size();
        for (String key : gameData.modelInfo.keySet()) {
            if (null != gameData.modelInfo.get(key).fileName) {
                assets.load(gameData.modelInfo.get(key).fileName, Model.class);
            }
        }

        SceneData.saveData(gameData);
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



    public void doneLoading() {

        for (String key : gameData.modelInfo.keySet()) {
            if (null != gameData.modelInfo.get(key).fileName) {
                gameData.modelInfo.get(key).model = assets.get(gameData.modelInfo.get(key).fileName, Model.class);
            }
        }
//        gameData.modelInfo.get("primitives").model = PrimitivesBuilder.primitivesModel; // maybe we don't need it
    }

    private static void createTestObjects(Engine engine) {

        Random rnd = new Random();

        int N_ENTITIES = 10;
        final int N_BOXES = 4;
        if (!useTestObjects) N_ENTITIES = 0;
        Vector3 size = new Vector3();

        PrimitivesBuilder boxBuilder = PrimitivesBuilder.getBoxBuilder("boxTex");
        PrimitivesBuilder sphereBuilder = PrimitivesBuilder.getSphereBuilder("sphereTex");

        for (int i = 0; i < N_ENTITIES; i++) {

            size.set(rnd.nextFloat() + .1f, rnd.nextFloat() + .1f, rnd.nextFloat() + .1f);
            size.scl(2.0f); // this keeps object "same" size relative to previous primitivesModel size was 2x

            Vector3 translation =
                    new Vector3(rnd.nextFloat() * 10.0f - 5f, rnd.nextFloat() + 25f, rnd.nextFloat() * 10.0f - 5f);

            if (i < N_BOXES) {
                engine.addEntity(boxBuilder.create(size.x, translation, size));
            } else {
                engine.addEntity(sphereBuilder.create(size.x, translation, new Vector3(size.x, size.x, size.x)));
            }
        }
    }


    private static void addPickObject(Entity e, SceneData.GameObject gameObject) {

        if (gameObject.isPickable) {
            e.add(new PickRayComponent(gameObject.objectName)); // set the object name ... yeh pretty hacky
        }
    }


    // tmp special sauce for selectScreen
    private Array<Entity> charactersArray = new Array<Entity>();

    public Array<Entity> getCharactersArray(){
        return charactersArray;
    }


    public void buildScene(Engine engine) {

        for (String key : gameData.modelGroups.keySet()) {

            Gdx.app.log("SceneLoader", "key = " + key);
            /*
             * build a model group
             */
            SceneData.ModelGroup mg = gameData.modelGroups.get(key);
            SceneData.ModelInfo mgmdlinfo = gameData.modelInfo.get(mg.modelName);
            Model groupModel = null;

            if (null != mgmdlinfo) {

                groupModel = mgmdlinfo.model;

            } else if (null == mg.modelName && 0 == mg.gameObjects.size) {

                createTestObjects(engine); // tmp hakkkkk
                continue;
            }

            for (SceneData.GameObject gameObject : mg.gameObjects) {

                gameObject.isKinematic = mg.isKinematic;
                gameObject.isCharacter = mg.isCharacter;

                if (null != groupModel) {
                    // the purpose of binding a model to a group is for iterating all model nodes to support Node name globbing
                    /*
                     * searching the group model for the given gameObject.objectName* ...
                     * may not be super efficient and  ... increasing number of model nodes ???
                     * However walking the model is needed for globbed object name, not
                     * seeing a more efficient way right now.
                     */
                    /* load all nodes from model that match /objectName.*/
                    for (Node node : groupModel.nodes) {

                        String unGlobbedObjectName = gameObject.objectName.replaceAll("\\*$", "");

                        if (node.id.contains(unGlobbedObjectName)) {

                            buildGameObject(engine, gameObject, groupModel, node.id);
                        } // else  ... bail out if matched an un-globbed name ?
                    }
                } else {
                    buildGameObject(engine, gameObject, null, null);
                }
            }
        }
    }

    // gameObject.build() ?
    private void buildGameObject(Engine engine, SceneData.GameObject gameObject, Model groupModel, String nodeID) {

        SceneData.ModelInfo mdlinfo = gameData.modelInfo.get(gameObject.objectName);
        SceneData.GameObject.InstanceData id;
        int n = 0;

        if (null == gameObject.scale) {
            gameObject.scale = new Vector3(1, 1, 1);
        }

        do
        { // for (SceneData.GameObject.InstanceData i : gameObject.instanceData) ... but not because game objects may not populate any instance data
            id = null;

            if (gameObject.instanceData.size > 0) {
/*
instances should be same size/scale so that we can pass one collision shape to share between them
*/
                id = gameObject.instanceData.get(n++);
            }
// if (null != id) ... todo

            Entity e;

            if (null != nodeID) {

                btCollisionShape shape = null;

                e = new Entity();

                ModelInstance instance = ModelInstanceEx.getModelInstance(groupModel, nodeID);

                // https://stackoverflow.com/questions/21827302/scaling-a-modelinstance-in-libgdx-3d-and-bullet-engine
        /*
        scale is in parent object (not instances) because object should be able to share same bullet shape!
        HOWEVER ... seeing below that bullet comp is made with mesh, we still have duplicated meshes ;... :(
         */
///                if (null != gameObject.scale) // already asserted ... see above
                {
                    instance.nodes.get(0).scale.set(gameObject.scale);
                    instance.calculateTransforms();
                }

                // leave translation null if using translation from the model layout
                if (null != id) {
                    if (null != id.rotation) {
                        instance.transform.idt();
                        instance.transform.rotate(id.rotation);
                    }
                    if (null != id.translation) {
                        // nullify any translation from the model, apply instance translation
                        instance.transform.setTranslation(0, 0, 0);
                        instance.transform.trn(id.translation);
                    }
                }

                ModelComponent mc = new ModelComponent(instance);
                mc.isShadowed = gameObject.isShadowed; // disable shadowing of skybox)
                e.add(mc);

                if (null != gameObject.meshShape) {

                    if (gameObject.meshShape.equals("convexHullShape")) {

                        shape = MeshHelper.createConvexHullShape(instance.getNode(nodeID));

//                        int n = ((btConvexHullShape) shape).getNumPoints(); // GN: optimizes to 8 points for platform cube

                    } else if (gameObject.meshShape.equals("triangleMeshShape")) {

                        shape = Bullet.obtainStaticNodeShape(instance.getNode(nodeID), false);

                    } else if (gameObject.meshShape.equals("btBoxShape")) {

                        BoundingBox boundingBox = new BoundingBox();
                        Vector3 dimensions = new Vector3();
                        instance.calculateBoundingBox(boundingBox);

                        shape = new btBoxShape(boundingBox.getDimensions(dimensions).scl(0.5f));
                    }
                    // sphere?

                    float mass = gameObject.mass;
                    BulletComponent bc = new BulletComponent(shape, instance.transform, mass);
                    e.add(bc);

                    // special sauce here for static entity
                    if (gameObject.isKinematic) {  // if (0 == mass) ??
// set these flags in bullet comp?
                        bc.body.setCollisionFlags(
                                bc.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
                        bc.body.setActivationState(Collision.DISABLE_DEACTIVATION);
                    }
                }
            } else if (null == mdlinfo) {

                e = new Entity();

                ModelInstance instance =
                        ModelInstanceEx.getModelInstance(PrimitivesBuilder.getPrimitivesModel(), gameObject.objectName);

                e.add(new ModelComponent(instance));

                if (null != id.color)
                    ModelInstanceEx.setColorAttribute(e.getComponent(ModelComponent.class).modelInst, id.color, id.color.a); // kind of a hack ;)

                PrimitivesBuilder pb = PrimitivesBuilder.getPrimitiveBuilder(gameObject.objectName);

                btCollisionShape shape = null;

                if (null != pb) {
                    shape = pb.create(instance, gameObject.mass, id.translation, gameObject.scale);
                }

                if (!gameObject.isKinematic && null == gameObject.meshShape && 0 == gameObject.mass) {
                    shape.dispose(); ///  needed to get the model instance scaled etc. but we gotta silly shape to dispose()

                } else {
                    BulletComponent bc = new BulletComponent(shape, instance.transform, gameObject.mass);

                    if (0 == gameObject.mass) {
                        // special sauce here for static entity
// set these flags in bullet comp?
                        bc.body.setCollisionFlags(
                                bc.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
                        bc.body.setActivationState(Collision.DISABLE_DEACTIVATION);
                    }
                    e.add(bc);
                }
            } else {
                // look for a model file  named as the object
                Model model = mdlinfo.model;

                e = new Entity();

                // special sauce to hand off the model node
                ModelInstance inst = ModelInstanceEx.getModelInstance(model, model.nodes.get(0).id);
                inst.transform.trn(id.translation);
                e.add(new ModelComponent(inst));

                if (gameObject.mass > 0 /* useBulletComp */) {
                    btCollisionShape shape = MeshHelper.createConvexHullShape(model.meshes.get(0));
                    e.add(new BulletComponent(shape, inst.transform, gameObject.mass));
                }
            }

            if (gameObject.isSteerable) {
                e.add(new CharacterComponent());
            }
            if (gameObject.isCharacter && null != charactersArray) {
                charactersArray.add(e);
            }

            addPickObject(e, gameObject);

//if (null != e) // assert
            engine.addEntity(e);

        } while (null != id && n < gameObject.instanceData.size);
    }

    @Override
    public void dispose() {

        // The Model owns the meshes and textures, to dispose of these, the Model has to be disposed. Therefor, the Model must outlive all its ModelInstances
        //  Disposing the file will automatically make all instances invalid!
        assets.dispose();

// new test file writer
        SceneData cpGameData = new SceneData();

        for (String key : gameData.modelGroups.keySet()) {

            SceneData.ModelGroup mg = new SceneData.ModelGroup(key /* gameData.modelGroups.get(key).groupName */);

            for (SceneData.GameObject o : gameData.modelGroups.get(key).gameObjects) {

                SceneData.GameObject cpObject = new SceneData.GameObject(o.objectName, o.meshShape);

                for (SceneData.GameObject.InstanceData i : o.instanceData) {

                    cpObject.instanceData.add(i);
                }
                mg.gameObjects.add(cpObject);
            }

            cpGameData.modelGroups.put(key /* gameData.modelGroups.get(key).groupName */, mg);
        }
//        saveData(cpGameData);
    }
}
