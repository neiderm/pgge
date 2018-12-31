package com.mygdx.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.PickRayComponent;
import com.mygdx.game.util.MeshHelper;
import com.mygdx.game.util.ModelInstanceEx;
import com.mygdx.game.util.PrimitivesBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;


/**
 * Created by mango on 12/18/17.
 */

public class SceneLoader implements Disposable {

    private GameData gameData;
//    private static FileHandle fileHandle = Gdx.files.local("GameData.json");
    private static boolean useTestObjects = true;
    private AssetManager assets;

//    private static final float DEFAULT_TANK_MASS = 5.1f; // idkf


    public SceneLoader(String path) {

        gameData = new GameData();
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

        loadData(path);

        assets = new AssetManager();
/*
        assets.load("data/cubetest.g3dj", Model.class);
        assets.load("data/landscape.g3db", Model.class);
        assets. load("tanks/ship.g3db", Model.class);
        assets.load("tanks/panzerwagen.g3db", Model.class);
        assets.load("data/scene.g3dj", Model.class);
*/
        int i = gameData.modelInfo.values().size();
        for (String key : gameData.modelInfo.keySet()) {
            if (null != gameData.modelInfo.get(key).fileName) {
                assets.load(gameData.modelInfo.get(key).fileName, Model.class);
            }
        }

//        saveData();
    }

    public AssetManager getAssets(){
        return assets;
    }

    public static class ModelGroup {
        ModelGroup() {
        }

        ModelGroup(String groupName) {
        }

        ModelGroup(String groupName, String modelName) {
            this(groupName);
            this.modelName = modelName;
        }

        String modelName;
        Array<GameData.GameObject> gameObjects = new Array<GameData.GameObject>();
    }

    public static class ModelInfo {
        ModelInfo() {
        }

        ModelInfo(String fileName) {
            this.fileName = fileName;
        }

        String fileName;
        Model model;
    }

    public static class GameData {

        HashMap<String, ModelGroup> modelGroups = new HashMap<String, ModelGroup>();
        HashMap<String, ModelInfo> modelInfo = new HashMap<String, ModelInfo>();

        static class GameObject {
            GameObject() {
            }

            GameObject(String objectName, String meshShape) {
                this.objectName = objectName;
                this.meshShape = meshShape;
                this.isShadowed = true;
                this.isKinematic = true;
                this.scale = new Vector3(1, 1, 1); // placeholder
            }
            static class InstanceData {
                InstanceData() {
                }

                InstanceData(Vector3 translation, Quaternion rotation) {
                    this.translation = new Vector3(translation);
                    this.rotation = new Quaternion(rotation);
                    this.color = Color.CORAL;
                }

                Quaternion rotation;
                Vector3 translation;
                Color color;
            }

            Array<InstanceData> instanceData = new Array<InstanceData>();
            String objectName;
//            Vector3 translation; // needs to be only per-instance
            Vector3 scale; // NOT per-instance, all instances should be same scale (share same collision Shape)
            float mass;
            String meshShape; // triangleMeshShape, convexHullShape
            boolean isKinematic;
            boolean isShadowed;
        }
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

    private void saveData(GameData data) {
        Json json = new Json();
        FileHandle fileHandle = Gdx.files.local("GameData_out.json");
        if (gameData != null) {
//            fileHandle.writeString(Base64Coder.encodeString(json.prettyPrint(gameData)), false);
            fileHandle.writeString(json.prettyPrint(data), false);
            //System.out.println(json.prettyPrint(gameData));
        }
    }

    private void loadData(String path) {
        Json json = new Json();
        FileHandle fileHandle = Gdx.files.internal(path);
        //        gameData = json.fromJson(GameData.class, Base64Coder.decodeString(fileHandle.readString()));
        gameData = json.fromJson(GameData.class, fileHandle.readString());
    }


    public void doneLoading() {

        for (String key : gameData.modelInfo.keySet()) {
            if (null != gameData.modelInfo.get(key).fileName) {
                gameData.modelInfo.get(key).model = assets.get(gameData.modelInfo.get(key).fileName, Model.class);
            }
        }
        gameData.modelInfo.get("primitives").model = PrimitivesBuilder.primitivesModel; // special sauce hakakakakakak
    }

    private final Random rnd = new Random();

    public void onPlayerPicked(Engine engine) {

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


        Vector3 t = new Vector3(-10, +15f, -15f);
        Vector3 s = new Vector3(2, 3, 2); // scale (w, h, d, but usually should w==d )
        if (useTestObjects) {
            // assert (s.x == s.z) ... scaling of w & d dimensions should be equal
            addPickObject(engine, PrimitivesBuilder.getConeBuilder().create(5f, t, s));
            addPickObject(engine, PrimitivesBuilder.getCapsuleBuilder().create(5f, t, s));
            addPickObject(engine, PrimitivesBuilder.getCylinderBuilder().create(5f, t, s));
            addPickObject(engine, PrimitivesBuilder.getBoxBuilder().create(5f, t, s));
        }
    }


    private Entity buildTank(GameData.GameObject gameObject) {

        Entity e = null;
        Model model = gameData.modelInfo.get(gameObject.objectName).model;

        if (0 == gameObject.instanceData.size) {
            // no instance data ... default translation etc.
        } else {
            for (GameData.GameObject.InstanceData i : gameObject.instanceData) {

                e = new Entity();

                // leave translation null if using translation from the file layout ??
                ModelInstance inst = new ModelInstance(model);
// can be loaded this way but the tank can't ;(
                //        ModelInstance inst = ModelInstanceEx.getModelInstance(model, "ship");
                inst.transform.trn(i.translation);
                e.add(new ModelComponent(inst));

                btCollisionShape shape = MeshHelper.createConvexHullShape(model, true);
                e.add(new BulletComponent(shape, inst.transform, gameObject.mass));
            }
        }
        return e;
    }


    private void buildObject(Engine engine, GameData.GameObject gameObject, Model model) {

        if (0 == gameObject.instanceData.size) {
                        // no instance data ... default translation etc.

            if (gameObject.objectName.endsWith("*")){
                /* load all nodes from model that match /objectName.*/
                for (Iterator<Node> iterator = model.nodes.iterator(); iterator.hasNext();)
                {
                    Node node = iterator.next();
                    String gameObjectName = gameObject.objectName;
                    String unGlobbedObjectName = gameObjectName.replaceAll("\\*$", "");
                    if (node.id.contains(unGlobbedObjectName )) {

                        Entity e = buildObjectInstance(gameObject, null, model, node.id);
                        engine.addEntity(e);
                    }
                }
            } else {
                Entity e = buildObjectInstance(gameObject, null, model, gameObject.objectName);
                engine.addEntity(e);
            }
        } else {
            for (GameData.GameObject.InstanceData i : gameObject.instanceData) {
/*
instances should be same size/scale so that we can pass one collision shape to share between them
 */
                Entity e = buildObjectInstance(gameObject, i, model, gameObject.objectName);
                engine.addEntity(e);
            }
        }
    }

    /* could end up "modelGroup.build()" */
    private Entity buildObjectInstance(
            GameData.GameObject gameObject, GameData.GameObject.InstanceData i, Model model, String node) {

//        Model model; // if null then get model reference from object

        btCollisionShape shape = null;
//        String node = gameObject.objectName;

/// BaseEntityBuilder.load ??
        Entity e = new Entity();
        ModelInstance instance = ModelInstanceEx.getModelInstance(model, node);

        /*
Note only skySphere object using this right now
        scale is in parent object (not instances) because object should be able to share same bullet shape!
        HOWEVER ... seeing below that bullet comp is made with mesh, we still have duplicated meshes ;... :(
         */
        if (null != gameObject.scale){
// https://stackoverflow.com/questions/21827302/scaling-a-modelinstance-in-libgdx-3d-and-bullet-engine
            instance.nodes.get(0).scale.set(gameObject.scale);
            instance.calculateTransforms();
        }

        // leave translation null if using translation from the model layout
        if (null != i ) {
            if (null != i.rotation) {
                instance.transform.idt();
                instance.transform.rotate(i.rotation);
            }
            if (null != i.translation) {
                // nullify any translation from the model, apply instance translation
                instance.transform.setTranslation(0, 0, 0);
                instance.transform.trn(i.translation);
            }
        }

        ModelComponent mc = new ModelComponent(instance);
        mc.isShadowed = gameObject.isShadowed; // disable shadowing of skybox)
        e.add(mc);

        if (gameObject.meshShape.equals("none")) {
            // no mesh, no bullet
        } else {
            if (gameObject.meshShape.equals("convexHullShape")) {
                shape = MeshHelper.createConvexHullShape(instance.getNode(node));
                int n = ((btConvexHullShape) shape).getNumPoints(); // GN: optimizes to 8 points for platform cube
            } else if (gameObject.meshShape.equals("triangleMeshShape")) {
                shape = Bullet.obtainStaticNodeShape(instance.getNode(node), false);
            } else if (gameObject.meshShape.equals("btBoxShape")) {
                BoundingBox boundingBox = new BoundingBox();
                Vector3 dimensions = new Vector3();
                instance.calculateBoundingBox(boundingBox);
                shape = new btBoxShape(boundingBox.getDimensions(dimensions).scl(0.5f));
            }

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

        return e;
    }


    public void getCharacters(Array<Entity> characters ){

        for (GameData.GameObject gameObject : gameData.modelGroups.get("characters").gameObjects) {

            Entity e = buildTank(gameObject);

            characters.add(e);
        }
    }


    /* normally loading entire model.meshparts and tying together w/ single triangle mesh shape
     but trans and rotation are provided for models that I don't have object to modify or if
     instances are used (don't anticipate instances being typical on this one)
     */
    private Entity /* createLandscape*/ objectFromMeshParts(Model model, Vector3 trans, Quaternion rotation) {

        Entity e = new Entity();
        ModelInstance inst = new ModelInstance(model);
        inst.transform.idt().rotate(rotation).trn(trans);
        e.add(new ModelComponent(inst));

        btCollisionShape         shape = new btBvhTriangleMeshShape(model.meshParts);

        // obtainStaticNodeShape works for terrain mesh - selects a triangleMeshShape  - but is overkill. anything else
//        btCollisionShape shape = Bullet.obtainStaticNodeShape(model.nodes);
        BulletComponent bc = new BulletComponent(shape, inst.transform, 0f);
        e.add(bc);

        // special sauce here for static entity
// set these flags in bullet comp?
        bc.body.setCollisionFlags(
                bc.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
        bc.body.setActivationState(Collision.DISABLE_DEACTIVATION);

        return e;
    }

    public Entity buildObjectByName(Engine engine, String name) {
        Entity e = null;
        for (GameData.GameObject gameObject : gameData.modelGroups.get("tanks").gameObjects) {
            if (null != gameObject) {
                if (gameObject.objectName.contains(name)) {
                    e = buildTank(gameObject);
                    addPickObject(engine, e, gameObject.objectName);
                    break;
                }
            }
        }
        return e;
    }

    public void buildTanks(Engine engine){
        for (GameData.GameObject gameObject : gameData.modelGroups.get("tanks").gameObjects) {
            Entity e = buildTank(gameObject);
            addPickObject(engine, e, gameObject.objectName);
        }
    }

    public void buildArena(Engine engine) {

        Model model;
        ModelInfo mi;

        mi = gameData.modelInfo.get("scene");
        if (null != mi) {
            model = gameData.modelInfo.get("scene").model;
            for (GameData.GameObject gameObject : gameData.modelGroups.get("scene").gameObjects) {
                buildObject(engine, gameObject, model);
            }
        }

        mi = gameData.modelInfo.get("objects");
        if (null != mi) {
            model = gameData.modelInfo.get("objects").model;
            for (GameData.GameObject gameObject : gameData.modelGroups.get("objects").gameObjects) {
                buildObject(engine, gameObject, model);
            }
        }

        /*
         * refer to buildTank: entire model is taken and bullet shape applied to whole model.
         * so each model is a chunk of or possibly even the entire arena .
         * Allowing them to be instanced and offset/rotated as can be done with "ordinary" objects.
         */
        for (GameData.GameObject gameObject : gameData.modelGroups.get("areeners").gameObjects) {
            Entity e;

            if (0 == gameObject.instanceData.size) {
                // no instance data ... default translation etc.
            } else {
                for (GameData.GameObject.InstanceData i : gameObject.instanceData) {

                    e = objectFromMeshParts(
                            gameData.modelInfo.get(gameObject.objectName).model, i.translation, i.rotation);
                    engine.addEntity(e);
                }
            }
        }

        ModelGroup mmm = gameData.modelGroups.get("primitives");
        for (GameData.GameObject o : mmm.gameObjects) {

            PrimitivesBuilder pb = null;
            if (o.objectName.contains("box")) {
// bulletshape given in file but get box builder is tied to it already
                pb = PrimitivesBuilder.getBoxBuilder(o.objectName); // this constructor could use a size param ?
            }
            if (o.objectName.contains("sphere")) {
// bulletshape given in file but get Sphere builder is tied to it already
                pb = PrimitivesBuilder.getSphereBuilder(o.objectName); // this constructor could use a size param ?
            }
            if (o.objectName.contains("cylinder")) {
                pb = PrimitivesBuilder.getCylinderBuilder(); // currently I don't have a cylinder builder with name parameter for texturing
            }

            if (null != pb) {
                Vector3 scale = o.scale;
//                if (null == scale) scale = new Vector3(1, 1,1);
                for (GameData.GameObject.InstanceData i : o.instanceData) {
                    Entity e = pb.create(o.mass, i.translation, scale);
                    if (null != i.color)
                        ModelInstanceEx.setColorAttribute(e.getComponent(ModelComponent.class).modelInst, i.color, i.color.a); // kind of a hack ;)
                    engine.addEntity(e);
                }
                // hmmm .. here is for the skybox - generalize it? (anything else, load it this way?)
            } else // if (o.objectName.equals("skySphere"))
                 {
                // hack this is here only to get the model name
                buildObject(engine, o, PrimitivesBuilder.primitivesModel);
            }
        }
    }


    @Override
    public void dispose() {

        // The Model owns the meshes and textures, to dispose of these, the Model has to be disposed. Therefor, the Model must outlive all its ModelInstances
        //  Disposing the file will automatically make all instances invalid!
        assets.dispose();


// new test file writer
        GameData cpGameData = new GameData();

        for (String key : gameData.modelGroups.keySet()) {

            ModelGroup mg = new ModelGroup(key /* gameData.modelGroups.get(key).groupName */);

            for (GameData.GameObject o : gameData.modelGroups.get(key).gameObjects) {

                GameData.GameObject cpObject =  new GameData.GameObject(o.objectName, o.meshShape);

                for (GameData.GameObject.InstanceData i : o.instanceData) {

                    cpObject.instanceData.add(i);
                }
                mg.gameObjects.add(cpObject);
            }
            cpGameData.modelGroups.put(key /* gameData.modelGroups.get(key).groupName */, mg);

        }
        saveData(cpGameData);
    }

    private static Entity addPickObject(Engine engine, Entity e) {
        return addPickObject(engine, e, null);
    }

    private static Entity addPickObject(Engine engine, Entity e, String objectName) {

        e.add(new PickRayComponent(objectName)); // set the object name ... yeh pretty hacky
        engine.addEntity(e);
        return e; // for method call chaining
    }
}
