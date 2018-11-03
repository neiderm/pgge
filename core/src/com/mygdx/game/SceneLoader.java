package com.mygdx.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
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
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.PickRayComponent;
import com.mygdx.game.util.BaseEntityBuilder;
import com.mygdx.game.util.MeshHelper;
import com.mygdx.game.util.ModelInstanceEx;
import com.mygdx.game.util.PrimitivesBuilder;

import java.util.HashMap;
import java.util.Random;


/**
 * Created by mango on 12/18/17.
 */

public class SceneLoader implements Disposable {

    public static SceneLoader globalInstance = null;
    public static GameData gameData;
    private static FileHandle fileHandle = Gdx.files.local("GameData.json");
    private static boolean useTestObjects = true;
    private static AssetManager assets;
    private static Model landscapeModel;
    private static Model tankModel;  // opengameart.org/content/tankcar
    private static Model shipModel;
    private static Model sceneModel;
    private static Model testCubeModel;

    private static final float DEFAULT_TANK_MASS = 5.1f; // idkf


    private SceneLoader() {
        //        throw new GdxRuntimeException("not allowed, use bulletWorld = BulletWorld.getInstance() ");
    }

    public static SceneLoader getInstance() {

        if (null == globalInstance) {
            globalInstance = new SceneLoader();
        }
        return globalInstance;
    }

    public AssetManager init() {

        PrimitivesBuilder.init();

        gameData = new GameData();
///*
        ModelGroup tanksGroup = new ModelGroup("tanks");
        tanksGroup.gameObjects.add(new GameData.GameObject("ship", "tanks/ship.g3db", new Vector3(-1, 13f, -5f)));
        tanksGroup.gameObjects.add(new GameData.GameObject("tank", "tanks/panzerwagen.g3db", new Vector3(1, 11f, -5f)));
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

        gameData.modelInfo.put("scene", new ModelInfo("scene", "data/scene.g3dj"));
        gameData.modelInfo.put("landscape", new ModelInfo("landscape", "data/landscape.g3db"));
        gameData.modelInfo.put("ship", new ModelInfo("ship", "tanks/ship.g3db"));
        gameData.modelInfo.put("tank", new ModelInfo("tank", "tanks/panzerwagen.g3db"));
        gameData.modelInfo.put("objects", new ModelInfo("objects", "data/cubetest.g3dj"));
        gameData.modelInfo.put("primitives", new ModelInfo("primitivesModel", null));
//*/
        saveData(); // tmp: saving to temp file, don't overwrite what we have

//        initializeGameData();

        loadData();

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

        return assets;
    }


    public static class ModelGroup {
        ModelGroup() {
        }
        ModelGroup(String groupName){
            this.groupName = groupName; // could be null if the objects in the group load their own individual models e.g. tanks
        }
        ModelGroup(String groupName, String modelName) {
            this(groupName );
            this.modelName = modelName;
        }
        String modelName;
        String groupName;
        Array<GameData.GameObject> gameObjects = new Array<GameData.GameObject>();
    }

    public static class ModelInfo {
        ModelInfo(){}
        ModelInfo(String modelName, String fileName) {
            this.modelName = modelName;
            this.fileName = fileName;
        }

        String modelName;
        String fileName;
        Model model;
    }

    public static class GameData {

//        Array<ModelGroup> modelGroups = new Array<ModelGroup>();
HashMap<String, ModelGroup> modelGroups = new HashMap<String, ModelGroup>();
        HashMap<String, ModelInfo> modelInfo = new HashMap<String, ModelInfo>();

        static class GameObject {
            GameObject() {
            }

            GameObject(String objectName, String meshShape) {
                this.objectName = objectName;
                this.modelName = null;
                this.translation = null;
                this.meshShape = meshShape;
                this.isShadowed = true;
                this.isKinematic = true;
            }

            GameObject(String objectName, String modelName, Vector3 translation) {
                this(objectName, "convexHullShape"); // convex hull shape default
                this.modelName = modelName;
                this.translation = new Vector3(translation);
            }

            String modelName;
            String objectName;
            Vector3 translation;
            String meshShape; // triangleMeshShape, convexHullShape
            boolean isKinematic;
            boolean isShadowed;
        }
    }

    /*
        http://niklasnson.com/programming/network/tips%20and%20tricks/2017/09/15/libgdx-save-and-load-game-data.html
    */

    public void initializeGameData() {
        if (!fileHandle.exists()) {
            gameData = new GameData();

            saveData();
        } else {
            loadData();
        }
    }

    public void saveData() {
        Json json = new Json();
        FileHandle fileHandle = Gdx.files.local("GameData_out.json");
        if (gameData != null) {
            //fileHandle.writeString(Base64Coder.encodeString(json.prettyPrint(gameData)), false);
            fileHandle.writeString(json.prettyPrint(gameData), false);
            //System.out.println(json.prettyPrint(gameData));
        }
    }

    public void loadData() {
        Json json = new Json();
//        gameData = json.fromJson(GameData.class, Base64Coder.decodeString(fileHandle.readString()));
        gameData = json.fromJson(GameData.class, fileHandle.readString());
    }


    public void doneLoading() {
/*
        landscapeModel = assets.get("data/landscape.g3db", Model.class);
        tankModel = assets.get("tanks/panzerwagen.g3db", Model.class);
        shipModel = assets.get("tanks/ship.g3db", Model.class);
        sceneModel = assets.get("data/scene.g3dj", Model.class);
        testCubeModel = assets.get("data/cubetest.g3dj", Model.class);
*/
        for (String key : gameData.modelInfo.keySet()) {
            if (null != gameData.modelInfo.get(key).fileName) {
                gameData.modelInfo.get(key).model = assets.get(gameData.modelInfo.get(key).fileName, Model.class);
            }
        }
        gameData.modelInfo.get("primitives").model = PrimitivesBuilder.primitivesModel; // special sauce hakakakakakak
// tmp
        landscapeModel = gameData.modelInfo.get("landscape").model;
        tankModel = gameData.modelInfo.get("tank").model;
        shipModel = gameData.modelInfo.get("ship").model;
        sceneModel = gameData.modelInfo.get("scene").model ;
        testCubeModel = gameData.modelInfo.get("objects").model;
    }

    public void createObjects(Engine engine) {

        int N_ENTITIES = 10;
        final int N_BOXES = 4;
        if (!useTestObjects) N_ENTITIES = 0;
        Vector3 size = new Vector3();
        Random rnd = new Random();

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

            addPickObject(engine, PrimitivesBuilder.loadCone(5f, t, s));
            addPickObject(engine, PrimitivesBuilder.loadCapsule(5f, t, s));
            addPickObject(engine, PrimitivesBuilder.loadCylinder(5f, t, s));
            addPickObject(engine, PrimitivesBuilder.loadBox(5f, t, s));
        }
    }

    public Entity createTank(Engine engine, Vector3 trans) {

        Model model = tankModel;

        Entity e = new Entity();

        // leave translation null if using translation from the file layout ??
        ModelInstance inst = new ModelInstance(model);
        inst.transform.trn(trans);
        e.add(new ModelComponent(inst));

        btCollisionShape shape = MeshHelper.createConvexHullShape(model, true);
        e.add(new BulletComponent(shape, inst.transform, DEFAULT_TANK_MASS));

        addPickObject(engine, e);

        return e;
    }

    public Entity createShip(Engine engine, Vector3 trans) {

        Model model = shipModel;

        Entity e = new Entity();

        // leave translation null if using translation from the file layout ??
        ModelInstance inst = new ModelInstance(model);
        inst.transform.trn(trans);
        e.add(new ModelComponent(inst));

        btCollisionShape shape = MeshHelper.createConvexHullShape(model, true);
        e.add(new BulletComponent(shape, inst.transform, DEFAULT_TANK_MASS));

        addPickObject(engine, e);

        return e;
    }

    public Entity _createShip(Engine engine, Vector3 trans) {

        Model model = testCubeModel;
        String node = "ship";

        Entity e = new Entity();

        // leave translation null if using translation from the file layout ??
        ModelInstance inst = ModelInstanceEx.getModelInstance(model, node);
        inst.transform.trn(trans);
        e.add(new ModelComponent(inst));

        btCollisionShape shape = MeshHelper.createConvexHullShape(inst.getNode(node));
        e.add(new BulletComponent(shape, inst.transform, DEFAULT_TANK_MASS));

        addPickObject(engine, e);

        return e;
    }


    private Entity _createLandscape(Vector3 trans) {

        Model model = landscapeModel;

        Entity e = new Entity();
        ModelInstance inst = new ModelInstance(model);

        // put the landscape at an angle so stuff falls of it...
        inst.transform.idt().rotate(new Vector3(1, 0, 0), 20f).trn(trans);
        e.add(new ModelComponent(inst));

        //            shape = new btBvhTriangleMeshShape(file.meshParts);
        // obtainStaticNodeShape works for terrain mesh - selects a triangleMeshShape  - but is overkill. anything else
        btCollisionShape shape = Bullet.obtainStaticNodeShape(model.nodes);
        e.add(new BulletComponent(shape, inst.transform, 0f));

        // special sauce here for static entity
        BulletComponent bc = e.getComponent(BulletComponent.class);

// set these flags in bullet comp?
        bc.body.setCollisionFlags(
                bc.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
        bc.body.setActivationState(Collision.DISABLE_DEACTIVATION);

        return e;
    }



    /* could end up "modelGroup.build()" */
    private Entity buildObject(GameData.GameObject gameObject, Model model){

//        Model model; // if null then get model reference from object

        Vector3 size = new Vector3(1, 1, 1);
        Vector3 trans = new Vector3(0, 0, 0);
        btCollisionShape shape = null;

        String node = gameObject.objectName;

/// BaseEntityBuilder.load ??
        Entity e = new Entity();
        ModelInstance instance = ModelInstanceEx.getModelInstance(model, node);

        e.add(new ModelComponent(instance));
///

        if (gameObject.meshShape.equals("none")){
          // no mesh, no bullet
        }
        else {
            if (gameObject.meshShape.equals("convexHullShape")) {
                shape = MeshHelper.createConvexHullShape(instance.getNode(node));
                int n = ((btConvexHullShape) shape).getNumPoints(); // GN: optimizes to 8 points for platform cube
            } else if (gameObject.meshShape.equals("triangleMeshShape")) {
                shape = Bullet.obtainStaticNodeShape(instance.getNode(node), false);
            }

            e.add(new BulletComponent(shape, instance.transform, 0f));

            // special sauce here for static entity
            BulletComponent bc = e.getComponent(BulletComponent.class);

            if (true == gameObject.isKinematic) {
// set these flags in bullet comp?
                bc.body.setCollisionFlags(
                        bc.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
                bc.body.setActivationState(Collision.DISABLE_DEACTIVATION);
            }
        }

        e.getComponent(ModelComponent.class).isShadowed = gameObject.isShadowed; // disable shadowing of skybox)

        return e;
    }

    public static Entity load(Model model, String rootNodeId) {
        // we can set trans default value as do-nothing 0,0,0 so long as .trn() is used (adds offset onto present trans value)
        return BaseEntityBuilder.load(model, rootNodeId, new Vector3(1, 1, 1), new Vector3(0, 0, 0));
    }


    public void buildArena(Engine engine) {

        final float yTrans = -10.0f;

        ModelGroup mg = gameData.modelGroups.get("scene");
        Model model = gameData.modelInfo.get("scene").model;

        for (GameData.GameObject gameObject : mg.gameObjects) {
            engine.addEntity(buildObject(gameObject, model));
        }

        loadDynamicEntiesByName(engine, testCubeModel, "Crate"); // platform THING

        // these are same size so this will allow them to share a collision shape
        Vector3 sz = new Vector3(2, 2, 2);
        PrimitivesBuilder bo = PrimitivesBuilder.getBoxBuilder("boxTex"); // this constructor could use a size param ?
// crate THINGs
///*
        engine.addEntity(bo.create(0.1f, new Vector3(0, 4, -15f), sz));
        engine.addEntity(bo.create(0.1f, new Vector3(-2, 4, -15f), sz));
        engine.addEntity(bo.create(0.1f, new Vector3(-4, 4, -15f), sz));
        engine.addEntity(bo.create(0.1f, new Vector3(0, 6, -15f), sz));
        engine.addEntity(bo.create(0.1f, new Vector3(-2, 6, -15f), sz));
        engine.addEntity(bo.create(0.1f, new Vector3(-4, 6, -15f), sz));
//*/

        float r = 16;
        Entity e;

        e = PrimitivesBuilder.getSphereBuilder("sphereTex").create(
                0, new Vector3(10, 5 + yTrans, 0), new Vector3(r, r, r));
//        e = PrimitivesBuilder.loadSphereTex(0, new Vector3(10, 5 + yTrans, 0), r);
        //        setObjectMatlTex(e.getComponent(ModelComponent.class).modelInst, sphereTex); // new Material(TextureAttribute.createDiffuse(sphereTex))
        engine.addEntity(e); // sphere THING

        e = PrimitivesBuilder.getBoxBuilder("boxTex").create(
                0, new Vector3(0, -4 + yTrans, 0), new Vector3(40f, 2f, 40f));
//        e = PrimitivesBuilder.loadBoxTex(0f, new Vector3(0, -4 + yTrans, 0), new Vector3(40f, 2f, 40f));
        //        setObjectMatlTex(e.getComponent(ModelComponent.class).modelInst, cubeTex); // new Material(TextureAttribute.createDiffuse(sphereTex))
        engine.addEntity(e);


// we can do primitive dynamic object (with 0 mass for platform)
        e = PrimitivesBuilder.loadBox(0f, new Vector3(0, 10, -5), new Vector3(4f, 1f, 4f));

        ModelInstanceEx.setColorAttribute(e.getComponent(ModelComponent.class).modelInst, Color.CORAL, 0.5f);
        engine.addEntity(e);
    }


    /*
     Model nodes loaded by name - can't assume they are same size, but fair to say they
     are all e.g. "cube00x" etc., so safe to assume all cube shapes. Thus, by default we
     create a cube shape sized by taking the bounding box of the mesh.
     */
    private static void loadDynamicEntiesByName(Engine engine, Model model, String rootNodeId) {

        for (int i = 0; i < model.nodes.size; i++) {

            String id = model.nodes.get(i).id;

            if (id.startsWith(rootNodeId)) {

                Entity e = new Entity();
                ModelInstance instance = ModelInstanceEx.getModelInstance(model, id);

                BoundingBox boundingBox = new BoundingBox();
                Vector3 dimensions = new Vector3();
                instance.calculateBoundingBox(boundingBox);

                e.add(new ModelComponent(instance));

                e.add(new BulletComponent(
                        new btBoxShape(boundingBox.getDimensions(dimensions).scl(0.5f)), instance.transform, 0.1f));

                engine.addEntity(e);
            }
        }
    }


    @Override
    public void dispose() {

        PrimitivesBuilder.dispose(); // hack, call static method

        // The Model owns the meshes and textures, to dispose of these, the Model has to be disposed. Therefor, the Model must outlive all its ModelInstances
        //  Disposing the file will automatically make all instances invalid!
        assets.dispose();
    }

    private static Entity addPickObject(Engine engine, Entity e) {

        e.add(new PickRayComponent());
        engine.addEntity(e);
        return e; // for method call chaining
    }
}
