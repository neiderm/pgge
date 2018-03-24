package com.mygdx.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btShapeHull;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.CharacterComponent;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.Managers.EntityFactory;
import com.mygdx.game.Managers.EntityFactory.BoxObject;
import com.mygdx.game.Managers.EntityFactory.GameObject;
import com.mygdx.game.Managers.EntityFactory.SphereObject;
import com.mygdx.game.Managers.EntityFactory.StaticEntiteeFactory;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

/**
 * Created by mango on 12/18/17.
 */

public class SceneLoader implements Disposable {

    public static final float fbxLoaderHack = 1;

    public static final SceneLoader instance = new SceneLoader();

    private static boolean useTestObjects = true;
    private static Model primitivesModel;
    private static final AssetManager assets;
    public static final Model landscapeModel;
    private static final Model shipModel;
    public static final Model sceneModel;
    public static Model boxTemplateModel;
    public static Model sphereTemplateModel;
    private static Model ballTemplateModel;
    private static Model tankTemplateModel;
    public static final Model testCubeModel;

    private SceneLoader() {
        //super();
    }

    public void init() { // ?????????

        //super.init();
    }

    static Vector3 tankSize = new Vector3(1, 1, 2);

    static {
        final ModelBuilder mb = new ModelBuilder();

        Texture cubeTex = new Texture(Gdx.files.internal("data/crate.png"), false);
        boxTemplateModel = mb.createBox(1f, 1f, 1f,
                new Material(TextureAttribute.createDiffuse(cubeTex)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        Texture sphereTex = new Texture(Gdx.files.internal("data/day.png"), false);
        sphereTemplateModel = mb.createSphere(1f, 1f, 1f, 16, 16,
                new Material(TextureAttribute.createDiffuse(sphereTex)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        Texture ballTex = new Texture(Gdx.files.internal("data/Ball8.jpg"), false);
        ballTemplateModel = mb.createSphere(1f, 1f, 1f, 16, 16,
                new Material(TextureAttribute.createDiffuse(ballTex)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        tankTemplateModel = mb.createBox(tankSize.x, tankSize.y, tankSize.z,
                new Material(TextureAttribute.createDiffuse(ballTex)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        assets = new AssetManager();
        assets.load("data/cubetest.g3dj", Model.class);
        assets.load("data/landscape.g3db", Model.class);
        assets.load("data/panzerwagen.g3db", Model.class); // https://opengameart.org/content/tankcar
//        assets.load("data/panzerwagen_3x3.g3dj", Model.class);
        assets.load("data/ship.g3dj", Model.class);
        assets.load("data/scene.g3dj", Model.class);
        assets.finishLoading();
        landscapeModel = assets.get("data/landscape.g3db", Model.class);
//        shipModel = assets.get("data/panzerwagen_3x3.g3dj", Model.class);
//        shipModel = assets.get("data/panzerwagen.g3db", Model.class);
        shipModel = assets.get("data/ship.g3dj", Model.class);
        sceneModel = assets.get("data/scene.g3dj", Model.class);
        testCubeModel = assets.get("data/cubetest.g3dj", Model.class);

        mb.begin();

        mb.node().id = "sphere";
        mb.part("sphere", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.GREEN))).sphere(1f, 1f, 1f, 10, 10);
        mb.node().id = "box";
        mb.part("box", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.BLUE))).box(1f, 1f, 1f);
        mb.node().id = "cone";
        mb.part("cone", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.YELLOW))).cone(1f, 2f, 1f, 10);
        mb.node().id = "capsule";
        mb.part("capsule", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.CYAN))).capsule(0.5f, 2f, 10);
        mb.node().id = "cylinder";
        mb.part("cylinder", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.MAGENTA))).cylinder(1f, 2f, 1f, 10);

        primitivesModel = mb.end();
    }


    public static void createEntities(Engine engine) {

        int N_ENTITIES = 5;
        final int N_BOXES = 2;
if (!useTestObjects) N_ENTITIES = 0;
        Vector3 tmpV = new Vector3(); // size
        Random rnd = new Random();

        for (int i = 0; i < N_ENTITIES; i++) {

            tmpV.set(rnd.nextFloat() + .1f, rnd.nextFloat() + .1f, rnd.nextFloat() + .1f);
            tmpV.scl(2.0f); // this keeps object "same" size relative to previous primitivesModel size was 2x

            Vector3 translation =
                    new Vector3(rnd.nextFloat() * 10.0f - 5f, rnd.nextFloat() + 25f, rnd.nextFloat() * 10.0f - 5f);

            if (i < N_BOXES) {
                engine.addEntity(
                        new BoxObject(tmpV, boxTemplateModel).create(tmpV.x, translation));
            } else {
                engine.addEntity(
                        new SphereObject(tmpV.x, sphereTemplateModel).create(tmpV.x, translation));
            }
        }


        Vector3 t = new Vector3(0, 0 + 25f, 0 - 5f);
        Vector3 s = new Vector3(1, 1, 1);
        if (useTestObjects) {
            engine.addEntity(
                    new GameObject(s, primitivesModel, "cone").create(rnd.nextFloat() + 0.5f, t,
                            new btConeShape(0.5f, 2.0f)));

            engine.addEntity(
                    new GameObject(s, primitivesModel, "capsule").create(rnd.nextFloat() + 0.5f, t,
                            new btCapsuleShape(0.5f, 0.5f * 2.0f)));

            engine.addEntity(
                    new GameObject(s, primitivesModel, "cylinder").create(rnd.nextFloat() + 0.5f, t,
                            new btCylinderShape(new Vector3(0.5f * 1.0f, 0.5f * 2.0f, 0.5f * 1.0f))));
/*
            engine.addEntity(
                    new GameObject(s, primitivesModel, "sphere").create(rnd.nextFloat() + 0.5f, t,
                            new btSphereShape(0.5f)));
*/
        }
    }

    public static void createTestObjects(Engine engine){

        BoxObject bo = new BoxObject(new Vector3(2, 2, 2), boxTemplateModel);
        engine.addEntity(bo.create(0.1f, new Vector3(0, 0 + 4, 0 - 5f)));
        engine.addEntity(bo.create(0.1f, new Vector3(-2, 0 + 4, 0 - 5f)));
        engine.addEntity(bo.create(0.1f, new Vector3(-4, 0 + 4, 0 - 5f)));
        engine.addEntity(bo.create(0.1f, new Vector3(0, 0 + 6, 0 - 5f)));
        engine.addEntity(bo.create(0.1f, new Vector3(-2, 0 + 6, 0 - 5f)));
        engine.addEntity(bo.create(0.1f, new Vector3(-4, 0 + 6, 0 - 5f)));

        Entity e;
        float yTrans = -10.0f;

        StaticEntiteeFactory<GameObject> staticFactory =
                new StaticEntiteeFactory<GameObject>();

        Vector3 trans = new Vector3(0, -4 + yTrans, 0);

        engine.addEntity(staticFactory.create(
                new BoxObject(new Vector3(40f, 2f, 40f), boxTemplateModel), trans));

        engine.addEntity(staticFactory.create(
                new SphereObject(16, sphereTemplateModel), new Vector3(10, 5 + yTrans, 0)));
///*
        engine.addEntity(staticFactory.create(
                new BoxObject(new Vector3(40f, 2f, 40f), primitivesModel, "box"), new Vector3(-15, 1, -20)));
//*/
/*
        Vector3 size = new Vector3(40, 2, 40);
        engine.addEntity(new EntityFactory.LandscapeObject().create(sceneModel, "Platform", new btBoxShape(size.cpy().scl(0.5f))));
*/
        if (true) { // this slows down bullet debug drawer considerably!
/*
            e = loadKinematicEntity(
                    engine, landscapeModel, null, new btBvhTriangleMeshShape(landscapeModel.meshParts), null, null);
*/
            e = new EntityFactory.LandscapeObject().create(landscapeModel, new Matrix4());
            engine.addEntity(e);

            // put the landscape at an angle so stuff falls of it...
            ModelInstance inst = e.getComponent(ModelComponent.class).modelInst;
            inst.transform.idt().rotate(new Vector3(1, 0, 0), 20f).trn(0, 0 + yTrans, 0);

            e.getComponent(BulletComponent.class).body.setWorldTransform(inst.transform);
        }

// TODO: intatiate object as dynamic, let it fall, then let it rest as static (take out of dynamics world)
    }


    /*
    a character object that tracks the given "node" ...
     */
    public static Entity createChaser1(Engine engine, Matrix4 tgtTransform) {
        /*
          Right now we're moving (translating) it directly, according to a PI loop idea.
          ("Plant" output is simply an offset displacement added to present position).
          This is fine IF the "camera body" is not colliding into another phsics body!

          Regarding kinematic bodyies ...

          "Such an object that does move, but does not respond to collisions, is called a kinematic
          body. In practice a kinematic body is very much like a static object, except that you can
          change its location and rotation through code."

          BUT I don't think kinematc is exactly right thing for my camera, because I don't want the
          camera to affect other phys objects (like the way the ground that does not respond to
          collisions but nonetheless influences objects with forces that can stop them falling
          or them bounce roll etc.)
        */

        Entity e = new GameObject(new Vector3(0.5f, 0.5f, 0.5f),
                primitivesModel, "sphere").create(new Vector3(0, 15f, -5f));

        // static entity not use motion state so just set the scale on it once and for all
        ModelComponent mc = e.getComponent(ModelComponent.class);
        mc.modelInst.transform.scl(mc.scale);
        mc.modelInst.userData = 0xaa55;
        e.add(new CharacterComponent(
                new PIDcontrol(tgtTransform,
                        mc.modelInst.transform,
                        new Vector3(0, 2, 3 * fbxLoaderHack),
                        0.1f, 0, 0)));

        engine.addEntity(e);
        return e;
    }

    /*
        a character object that tracks the given "node" ...
     */
    public static Entity createChaser2(Engine engine, Vector3 node) {

        /*
          (WE're using/calculating PID error term in 3D, so we could normalize this value
          into a unit vector that would provde appropriate direction vector for applied force!
          (NOTE: the camera "body" should NOT exert foces on other phys objects, so I THINK that
          this is achievable simply by using mass of 0?)

I make it a full phsics object controlled by forces. As a full fledged dynamics object,
have to deal with getting "caught" in other phys structures ... possibly build some "flotation" into
camera and have it dragged along by player as a sort of tether. Or we could use raycast between
player and camera, and if obstacle between, we "break" the tether, allow only force on camera to
be it's "buoyancy", and let if "float up" until free of interposing obstacles .
        */

        final float r = 4.0f;
        Entity e = new GameObject(new Vector3(r, r, r), primitivesModel, "sphere").create(
                0.01f, new Vector3(0, 15f, -5f), new btSphereShape(r / 2f));

        btRigidBody body = e.getComponent(BulletComponent.class).body;
//        e.add(new CharacterComponent(                new PhysicsPIDcontrol(body, node, 0.1f, 0, 0)));

        engine.addEntity(e);
        return e;
    }


    public static void loadDynamicEntiesByName(
            Engine engine, Model model, String node, float mass, btCollisionShape shape ) {

        for (int i = 0; i < model.nodes.size; i++) {
            String id = model.nodes.get(i).id;
            if (id.startsWith(node)) {
                engine.addEntity(loadDynamicEntity(model, shape, id, mass, null, null));
            }
        }
    }

    public static Entity loadStaticEntity(Model model, String node) {

        Entity e = new GameObject(null, null, null).create();

        if (null != node) {
            ModelInstance instance;
            instance = getModelInstance(model, node);
            e.add(new ModelComponent(instance, null));
        } else {
            e.add(new ModelComponent(model, new Matrix4()));
        }

        return e;
    }

    public static Entity loadKinematicEntity(
            Model model, String nodeID, btCollisionShape shape, Vector3 trans, Vector3 size) {

        Entity entity = loadDynamicEntity(model, shape, nodeID, 0, trans, size);

        // called loadDynamicEntity w/ mass==0, so it's BC will NOT have a motionState (which is what we
        // want for this object) so we do need to update the bc.body with the location vector we got from the model
        Vector3 tmp = new Vector3();
        entity.getComponent(ModelComponent.class).modelInst.transform.getTranslation(tmp);
        BulletComponent bc = entity.getComponent(BulletComponent.class);

if (null == trans)
{
    bc.body.translate(tmp); // if translation param not given, need to sync body /w mesh instance
}

        bc.body.setCollisionFlags(
                bc.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
        bc.body.setActivationState(Collision.DISABLE_DEACTIVATION);
        bc.sFlag = true;

        return entity;
    }


    public static Entity loadDynamicEntity(
            Model model, btCollisionShape shape, String nodeID, float mass, Vector3 translation, Vector3 size) {

        Entity e = loadStaticEntity(model, nodeID);
        ModelInstance instance = e.getComponent(ModelComponent.class).modelInst;

//        if (null != size){
//            instance.transform.scl(size); // if mesh must be scaled, do it before creating the hull shape
//        }

        if (null == shape) {
            if (null != nodeID) {
                shape = createConvexHullShape(instance.getNode(nodeID).parts.get(0).meshPart);
            }else{
                final Mesh mesh = model.meshes.get(0);
                shape = new btConvexHullShape(mesh.getVerticesBuffer(), mesh.getNumVertices(), mesh.getVertexSize());
//                shape = createConvexHullShape(mesh.getVerticesBuffer(), mesh.getNumVertices(), mesh.getVertexSize(), true);
            }
        }

        e.add(new BulletComponent(shape, instance.transform, mass));

        // set to translation here if you don't want what the primitivesModel gives you
        if (null != translation) {
            instance.transform.trn(translation);
            e.getComponent(BulletComponent.class).body.setWorldTransform(instance.transform);
        }

        if (null != size){
            instance.transform.scl(size); // if mesh must be scaled, do it before^H^H^H^H^H^H  ?????
        }

        return e;
    }


    /*
         * IN:
     *   Matrix4 transform: transform must be linked to Bullet Rigid Body
     * RETURN:
     *   ModelInstance ... which would be passed in to ModelComponent()
     */
    private static ModelInstance getModelInstance(Model model, String node) {

        Matrix4 transform = new Matrix4();
        ModelInstance instance = new ModelInstance(model, transform, node);
        Node modelNode = instance.getNode(node);

// https://xoppa.github.io/blog/loading-a-scene-with-libgdx/
        instance.transform.set(modelNode.globalTransform);
        modelNode.translation.set(0, 0, 0);
        modelNode.scale.set(1, 1, 1);
        modelNode.rotation.idt();
        instance.calculateTransforms();

        return instance;
    }


    /*
      http://badlogicgames.com/forum/viewtopic.php?t=24875&p=99976
     */
    private static btConvexHullShape createConvexHullShape(MeshPart meshPart) {

//        int numVertices  = meshPart.mesh.getNumVertices();    // no only works where our subject is the only node in the mesh!
        int numVertices = meshPart.size;
        int vertexSize = meshPart.mesh.getVertexSize();

        float[] nVerts = getVertices(meshPart);
        int size = numVertices * vertexSize; // nbr of floats

        FloatBuffer buffer = ByteBuffer.allocateDirect(size * 4).asFloatBuffer();
        BufferUtils.copy(nVerts, 0, buffer, size);

        btConvexHullShape shape = createConvexHullShape(buffer, numVertices, vertexSize, true);

        return shape;
    }

    /*
     * going off script ... found no other way to properly get the vertices from an "indexed" object
     */
    private static float[] getVertices(MeshPart meshPart) {

        int numMeshVertices = meshPart.mesh.getNumVertices();
        int numPartIndices = meshPart.size;
        short[] meshPartIndices = new short[numPartIndices];
        meshPart.mesh.getIndices(meshPart.offset, numPartIndices, meshPartIndices, 0);

        final int stride = meshPart.mesh.getVertexSize() / 4;
        float[] allVerts = new float[numMeshVertices * stride];
        meshPart.mesh.getVertices(0, allVerts.length, allVerts);

        float[] iVerts = new float[numPartIndices * stride];

        for (short n = 0; n < numPartIndices; n++) {
            System.arraycopy(allVerts, meshPartIndices[n] * stride, iVerts, n * stride, stride);
        }
        return iVerts;
    }

    /*
      https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/bullet/ConvexHullTest.java
     */
    private static btConvexHullShape createConvexHullShape(
            FloatBuffer points, int numPoints, int stride, boolean optimize) {

        final btConvexHullShape shape = new btConvexHullShape(points, numPoints, stride);

        if (!optimize) return shape;
        // now optimize the shape
        final btShapeHull hull = new btShapeHull(shape);
        hull.buildHull(shape.getMargin());
        final btConvexHullShape result = new btConvexHullShape(hull);
        // delete the temporary shape
        shape.dispose();
        hull.dispose();
        return result;
    }


    @Override
    public void dispose() {

        // The Model owns the meshes and textures, to dispose of these, the Model has to be disposed. Therefor, the Model must outlive all its ModelInstances
//  Disposing the primitivesModel will automatically make all instances invalid!
        sceneModel.dispose();
        tankTemplateModel.dispose();
        sphereTemplateModel.dispose();
        boxTemplateModel.dispose();
        ballTemplateModel.dispose();
        primitivesModel.dispose();
        assets.dispose();
    }
}
