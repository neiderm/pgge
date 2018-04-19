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
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.CharacterComponent;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.Components.PlayerComponent;

import java.util.Random;

/**
 * Created by mango on 12/18/17.
 */

public class SceneLoader implements Disposable {

    public static final SceneLoader instance = new SceneLoader();

    private static boolean useTestObjects = true;
    private static Model primitivesModel;
    private static final AssetManager assets;
    public static final Model landscapeModel;
    public static final Model shipModel;
    public static final Model sceneModel;
    public static final Model boxTemplateModel;
    public static final Model sphereTemplateModel;
    private static Model ballTemplateModel;
    private static Model tankTemplateModel;
    public static final Model testCubeModel;
    public static final ArrayMap<String, GameObject> primitiveModels;

    public static SizeableObject sphereTemplate;
    public static SizeableObject boxTemplate;
    public static SizeableObject coneTemplate;
    public static SizeableObject capsuleTemplate;
    public static SizeableObject cylinderTemplate;

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
//        assets.load("data/ship.g3dj", Model.class);
        assets.load("data/scene.g3dj", Model.class);
        assets.finishLoading();

        landscapeModel = assets.get("data/landscape.g3db", Model.class);
//        shipModel = assets.get("data/panzerwagen_3x3.g3dj", Model.class);
        shipModel = assets.get("data/panzerwagen.g3db", Model.class);
//        shipModel = assets.get("data/ship.g3dj", Model.class);
        sceneModel = assets.get("data/scene.g3dj", Model.class);
        testCubeModel = assets.get("data/cubetest.g3dj", Model.class);


        /*
          each anonymous game object element will implement
           load(Vector3 trans, Vector3 size)
          which will in turn correctly call
           load(Model model, String nodeID, btCollisionShape shape, Vector3 trans, Vector3 size)
         */
        primitiveModels = new ArrayMap<String, GameObject>(String.class, GameObject.class);


        // TODO: enumerate the primitives and struct their "id" along with the base unit dimensions of each.
        // array of GameObject, and then insert into the array an instance of each (anonymous sub-class) object
        // then push all over to GameObject or somewhere else suitable
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


        /* might need to settle for making these static instances in a singleton class to provide
        the templates for the scale-able primitive shapes (they can't be tied to a single model)
         */
/*
        primitiveModels.put("sphere", new GameObject(primitivesModel, "sphere") {
            @Override
            public Entity create(Vector3 trans, Vector3 size) {
                return load(this.model, this.rootNodeId, new btSphereShape(size.x * 0.5f), trans, size);
            }
        });
        primitiveModels.put("box", new GameObject(primitivesModel, "box") {
            @Override
            public Entity create(Vector3 trans, Vector3 size) {
                return load(this.model, this.rootNodeId, new btBoxShape(size.cpy().scl(0.5f)), trans, size);
            }
        });
*/
        sphereTemplate = new SizeableObject() {
            @Override
            public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                return load(model, rootNode, new btSphereShape(size.x * 0.5f), size, mass, trans);
            }
        };
        boxTemplate = new SizeableObject() {
            @Override
            public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                return load(model, rootNode, new btBoxShape(size.cpy().scl(0.5f)), size, mass, trans);
            }
        };
        coneTemplate = new SizeableObject() {
            @Override
            public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                return load(model, rootNode, new btConeShape(0.5f, 2.0f), size, mass, trans);
            }
        };
        capsuleTemplate = new SizeableObject() {
            @Override
            public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                return load(model, rootNode, new btCapsuleShape(0.5f, 0.5f * 2.0f), size, mass, trans);
            }
        };
        cylinderTemplate = new SizeableObject() {
            @Override
            public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                return load(model, rootNode, new btCylinderShape(new Vector3(0.5f * 1.0f, 0.5f * 2.0f, 0.5f * 1.0f)), size, mass, trans);
            }
        };
    }


    private static void setMaterialColor(Entity e){
        Random rnd = new Random();
        Array<Color> colors = new Array<Color>();

        colors.add(Color.WHITE);
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.YELLOW);
        colors.add(Color.PURPLE);

        int i = rnd.nextInt(colors.size - 1);

        ModelInstance inst = e.getComponent(ModelComponent.class).modelInst;
        Material mat = inst.materials.get(0);
        if (null == mat)
            return; // throw new GdxRuntimeException("not found");
        mat.set(ColorAttribute.createDiffuse(colors.get(i)));
    }

    public static void createEntities(Engine engine) {

        int N_ENTITIES = 10;
        final int N_BOXES = 4;
        if (!useTestObjects) N_ENTITIES = 0;
        Vector3 size = new Vector3();
        Random rnd = new Random();

        for (int i = 0; i < N_ENTITIES; i++) {

            size.set(rnd.nextFloat() + .1f, rnd.nextFloat() + .1f, rnd.nextFloat() + .1f);
            size.scl(2.0f); // this keeps object "same" size relative to previous primitivesModel size was 2x

            Vector3 translation =
                    new Vector3(rnd.nextFloat() * 10.0f - 5f, rnd.nextFloat() + 25f, rnd.nextFloat() * 10.0f - 5f);

            if (i < N_BOXES) {
                engine.addEntity(boxTemplate.create(boxTemplateModel, null, size.x, translation, size));
            } else {
                engine.addEntity(boxTemplate.create(sphereTemplateModel,
                        null, size.x, translation, new Vector3(size.x, size.x, size.x)));
            }
        }


        Vector3 t = new Vector3(0, 0 + 25f, 0 - 5f);
        Vector3 s = new Vector3(1, 1, 1); // scale
        if (useTestObjects) {
            engine.addEntity(coneTemplate.create(primitivesModel, "cone",0.5f, t, s));
            engine.addEntity(capsuleTemplate.create(primitivesModel, "capsule", 0.5f, t, s));
            engine.addEntity(cylinderTemplate.create(primitivesModel, "cylinder", 0.5f, t, s));
        }


        Entity skybox = GameObject.load(sceneModel, "space");
        skybox.getComponent(ModelComponent.class).isShadowed = false; // disable shadowing of skybox
        engine.addEntity(skybox);


        final float yTrans = -10.0f;

        if (true) { // this slows down bullet debug drawer considerably!

            Entity ls = GameObject.loadTriangleMesh(landscapeModel);
            engine.addEntity(ls);

            // put the landscape at an angle so stuff falls of it...
            ModelInstance inst = ls.getComponent(ModelComponent.class).modelInst;
            inst.transform.idt().rotate(new Vector3(1, 0, 0), 20f).trn(0, 0 + yTrans, 0);

            ls.getComponent(BulletComponent.class).body.setWorldTransform(inst.transform);
        }
    }

    public static Entity createPlayer(){

        Entity player;
        btCollisionShape boxshape = null; // new btBoxShape(new Vector3(0.5f, 0.35f, 0.75f)); // test ;)
        Model model = sceneModel;
        String node = "ship";
if (true) {
    node = null;
    model = shipModel;
    final Mesh mesh = shipModel.meshes.get(0);
    boxshape = EntityBuilder.createConvexHullShape(mesh.getVerticesBuffer(), mesh.getNumVertices(), mesh.getVertexSize(), true);
}
        player = GameObject.load(model, node, 5.1f, new Vector3(0, 15f, -5f), boxshape);
        player.add(new PlayerComponent());
        return player;
    }

    public static void createTestObjects(Engine engine){

        engine.addEntity(GameObject.load(testCubeModel, "Cube"));  // "static" cube
        engine.addEntity(GameObject.load(testCubeModel, "Platform001", null, null, new Vector3(1, 1, 1))); // somehow the convex hull shape works ok on this one (no gaps ??? ) ~~~ !!!

        loadDynamicEntiesByName(engine, testCubeModel, "Crate");

        // these are same size so this will allow them to share a collision shape
        Vector3 sz = new Vector3(2, 2, 2);
        GameObject bo = new GameObject(boxTemplateModel, sz, new btBoxShape(sz.cpy().scl(0.5f)));
        engine.addEntity(bo.create(0.1f, new Vector3(0, 0 + 4, 0 - 15f)));
        engine.addEntity(bo.create(0.1f, new Vector3(-2, 0 + 4, 0 - 15f)));
        engine.addEntity(bo.create(0.1f, new Vector3(-4, 0 + 4, 0 - 15f)));
        engine.addEntity(bo.create(0.1f, new Vector3(0, 0 + 6, 0 - 15f)));
        engine.addEntity(bo.create(0.1f, new Vector3(-2, 0 + 6, 0 - 15f)));
        engine.addEntity(bo.create(0.1f, new Vector3(-4, 0 + 6, 0 - 15f)));
/* this works, but it could share a single size Shape which it does not
        engine.addEntity(boxTemplate.create(boxTemplateModel, null,0.1f, new Vector3(0, 0 + 4, 0 - 15f), sz));
        engine.addEntity(boxTemplate.create(boxTemplateModel, null,0.1f, new Vector3(-2, 0 + 4, 0 - 15f), sz));
        engine.addEntity(boxTemplate.create(boxTemplateModel, null,0.1f, new Vector3(-4, 0 + 4, 0 - 15f), sz));
        engine.addEntity(boxTemplate.create(boxTemplateModel, null,0.1f, new Vector3(0, 0 + 6, 0 - 15f), sz));
        engine.addEntity(boxTemplate.create(boxTemplateModel, null,0.1f, new Vector3(-2, 0 + 6, 0 - 15f), sz));
        engine.addEntity(boxTemplate.create(boxTemplateModel, null,0.1f, new Vector3(-4, 0 + 6, 0 - 15f), sz));
*/
        float r = 16;
        final float yTrans = -10.0f;
        engine.addEntity(sphereTemplate.create(sphereTemplateModel, null,0,
                new Vector3(10, 5 + yTrans, 0), new Vector3(r, r, r)));
        engine.addEntity(boxTemplate.create(boxTemplateModel, null,0,
                new Vector3(0, -4 + yTrans, 0), new Vector3(40f, 2f, 40f)));
        engine.addEntity(boxTemplate.create(primitivesModel, "box",0,
                new Vector3(0, 10, -5), new Vector3(4f, 1f, 4f)));
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
                engine.addEntity(GameObject.load(model, id, 0.1f));
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

        Entity e = GameObject.load(primitivesModel, "sphere",
                new Vector3(10, 10, 10), new Vector3(0, 15f, -5f),
                0);

        // static entity not use motion state so just set the scale on it once and for all
        ModelComponent mc = e.getComponent(ModelComponent.class);

//        mc.modelInst.transform.scl(mc.scale);

        mc.modelInst.userData = 0xaa55;
        e.add(new CharacterComponent(
                new PIDcontrol(tgtTransform,
                        mc.modelInst.transform,
                        new Vector3(0, 2, 3),
                        0.1f, 0, 0)));

        engine.addEntity(e);
        return e;
    }

/*
 * extended objects ... if same shape instance could be used for multiple entities, then we need
 * the shape to be instanced in the constructor
 * Bounding box only works on mesh and doesn't work if we are scaling the mesh :(
 */
public static class SizeableObject extends GameObject {

    // needed for method override (make this class from an interface, derived GameObject?)
    public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
        return null;
    }

/*
 // @IN: material - call this only for objects that are colored by the material (not textured)

    public static Entity load(
            Model model, String nodeID, Material material, btCollisionShape shape, Vector3 size, float mass, Vector3 translation) {
        Entity e = load(model, nodeID, shape, size, mass, translation);
        setMaterialColor(e); // tmp test
        return e;
    }*/

    public static Entity load(
            Model model, String nodeID, btCollisionShape shape, Vector3 size, float mass, Vector3 translation) {
        Entity e;
        if (0 != mass)
            e = load(model, nodeID, size, mass, translation, shape);
        else
           e = load(model, nodeID, shape, translation, size);
        return e;
    }
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
