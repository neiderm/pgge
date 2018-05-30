package com.mygdx.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.UBJsonReader;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.CharacterComponent;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.Components.PickRayComponent;
import com.mygdx.game.Components.PlayerComponent;
import com.mygdx.game.util.BaseEntityBuilder;
import com.mygdx.game.util.BulletEntityBuilder;
import com.mygdx.game.util.MeshHelper;
import com.mygdx.game.util.ModelInstanceEx;
import com.mygdx.game.util.PrimitivesBuilder;

import java.util.Random;

/**
 * Created by mango on 12/18/17.
 */

public class SceneLoader /* implements Disposable */ {

    public static final SceneLoader instance = new SceneLoader();
//public static SceneLoader instance;

    private static boolean useTestObjects = true;
//    private static  AssetManager assets;
    private static  Model landscapeModel;
    private static  Model shipModel;
    private static  Model sceneModel;
    private static  Model testCubeModel;

    private static boolean loaded = false;

    private SceneLoader() {
        //super();
    }

    public static void /* SceneLoader */ init() {

        if (!loaded) {

            loaded = true;

            G3dModelLoader loader;
            loader = new G3dModelLoader(new JsonReader());
            testCubeModel = loader.loadModel(Gdx.files.internal("data/cubetest.g3dj"));
            sceneModel = loader.loadModel(Gdx.files.internal("data/scene.g3dj"));

            loader = new G3dModelLoader(new UBJsonReader());
            landscapeModel = loader.loadModel(Gdx.files.internal("data/landscape.g3db"));
            shipModel = loader.loadModel(Gdx.files.internal("data/panzerwagen.g3db"));
        }

//        if (null == instance){
//            instance = new SceneLoader();
//        }
//        return instance;
    }


    static {
/*
        assets = new AssetManager();
        assets.load("data/cubetest.g3dj", Model.class);
        assets.load("data/landscape.g3db", Model.class);
        assets.load("data/panzerwagen.g3db", Model.class); // https://opengameart.org/content/tankcar
//        assets.load("data/panzerwagen_3x3.g3dj", Model.class);
//        assets.load("data/ship.g3dj", Model.class);
        assets.load("data/scene.g3dj", Model.class);
        assets.finishLoading();

        landscapeModel = assets.get("data/landscape.g3db", Model.class);
//        shipModel = assets.get("data/panzerwagen_3x3.g3dj", Model.class);
        shipModel = assets.get("data/panzerwagen.g3db", Model.class);
//        shipModel = assets.get("data/ship.g3dj", Model.class);
        sceneModel = assets.get("data/scene.g3dj", Model.class);
        testCubeModel = assets.get("data/cubetest.g3dj", Model.class);
*/
    }



    public static void createEntities(Engine engine) {

        int N_ENTITIES = 10;
        final int N_BOXES = 4;
        if (!useTestObjects) N_ENTITIES = 0;
        Vector3 size = new Vector3();
        Random rnd = new Random();

        PrimitivesBuilder boxBuilder = PrimitivesBuilder.getBoxBuilder("data/crate.png");
        PrimitivesBuilder sphereBuilder = PrimitivesBuilder.getSphereBuilder("data/day.png");

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
            Entity pickObject = // tmp hack
                    addPickObject(engine, PrimitivesBuilder.loadBox(5f, t, s));

            ModelComponent tmp = pickObject.getComponent(ModelComponent.class);
            tmp.id = 65535;
        }


        Entity skybox = BaseEntityBuilder.load(sceneModel, "space");
        skybox.getComponent(ModelComponent.class).isShadowed = false; // disable shadowing of skybox
        engine.addEntity(skybox);


        if (true) { // this slows down bullet debug drawer considerably!
            Entity ls = BulletEntityBuilder.load(landscapeModel);
            engine.addEntity(ls);

            // put the landscape at an angle so stuff falls of it...
            final float yTrans = -10.0f;
            ModelInstance inst = ls.getComponent(ModelComponent.class).modelInst;
            inst.transform.idt().rotate(new Vector3(1, 0, 0), 20f).trn(0, 0 + yTrans, 0);

            ls.getComponent(BulletComponent.class).body.setWorldTransform(inst.transform);
        }
    }

    public static Entity createPlayer() {

        Entity player;
        btCollisionShape boxshape = null; // new btBoxShape(new Vector3(0.5f, 0.35f, 0.75f)); // test ;)
        Model model = sceneModel;
        String node = "ship";
        if (true) {
            node = null;
            model = shipModel;
            final Mesh mesh = shipModel.meshes.get(0);
            boxshape = MeshHelper.createConvexHullShape(mesh.getVerticesBuffer(), mesh.getNumVertices(), mesh.getVertexSize(), true);
        }
        player = BulletEntityBuilder.load(model, node, null, 5.1f, new Vector3(-1, 11f, -5f), boxshape);
        player.add(new PlayerComponent());
        return player;
    }

    public static void createTestObjects(Engine engine) {

        engine.addEntity(BaseEntityBuilder.load(testCubeModel, "Cube"));  // "static" cube
        engine.addEntity(BulletEntityBuilder.load(testCubeModel, "Platform001", null, null, new Vector3(1, 1, 1))); // somehow the convex hull shape works ok on this one (no gaps ??? ) ~~~ !!!

        loadDynamicEntiesByName(engine, testCubeModel, "Crate");

        // these are same size so this will allow them to share a collision shape
        Vector3 sz = new Vector3(2, 2, 2);
        PrimitivesBuilder bo = PrimitivesBuilder.getBoxBuilder("data/crate.png"); // this constructor could use a size param ?
        engine.addEntity(bo.create(0.1f, new Vector3(0, 4, -15f), sz));
        engine.addEntity(bo.create(0.1f, new Vector3(-2, 4, -15f), sz));
        engine.addEntity(bo.create(0.1f, new Vector3(-4, 4, -15f), sz));
        engine.addEntity(bo.create(0.1f, new Vector3(0, 6, -15f), sz));
        engine.addEntity(bo.create(0.1f, new Vector3(-2, 6, -15f), sz));
        engine.addEntity(bo.create(0.1f, new Vector3(-4, 6, -15f), sz));

        float r = 16;
        final float yTrans = -10.0f;
        Entity e;

        e = PrimitivesBuilder.getSphereBuilder("data/crate.png").create(
                0, new Vector3(10, 5 + yTrans, 0), new Vector3(r, r, r));
//        e = PrimitivesBuilder.loadSphereTex(0, new Vector3(10, 5 + yTrans, 0), r);
        //        setObjectMatlTex(e.getComponent(ModelComponent.class).modelInst, sphereTex); // new Material(TextureAttribute.createDiffuse(sphereTex))
        engine.addEntity(e);

        e = PrimitivesBuilder.getBoxBuilder("data/crate.png").create(
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
                engine.addEntity(BulletEntityBuilder.load(model, id, 0.1f));
            }
        }
    }

    /*
    a character object that tracks the given "node" ... a position (transform) plus offset for the
    PID control to track. So if it is handed the the world transform of the camera, it makes for a
    crude camera controller. "Plant" output is simply an offset displacement added to present position
    The sphere is just eye candy.
     */
    public static Entity createChaser1(Engine engine, Matrix4 tgtTransform) {

        float r = 0.5f;
        Entity e = PrimitivesBuilder.loadSphere(r, new Vector3(0, 15f, -5f));

        ModelComponent mc = e.getComponent(ModelComponent.class);

        mc.modelInst.userData = 0xaa55;
        e.add(new CharacterComponent(
                new PIDcontrol(tgtTransform, mc.modelInst.transform, new Vector3(0, 2, 3), 0.1f, 0, 0)));

        engine.addEntity(e);
        return e;
    }

//    @Override
    public static void dispose() {

        PrimitivesBuilder.dispose(); // hack, call static method

        // The Model owns the meshes and textures, to dispose of these, the Model has to be disposed. Therefor, the Model must outlive all its ModelInstances
//  Disposing the model will automatically make all instances invalid!
        landscapeModel.dispose();
        shipModel.dispose();
        sceneModel.dispose();
        testCubeModel.dispose();
//        assets.dispose();
    }

    private static Entity addPickObject(Engine engine, Entity e) {

        e.add(new PickRayComponent());
        engine.addEntity(e);

        return e; // for method call chaining
    }
}
