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
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.CharacterComponent;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.Components.PlayerComponent;
import com.mygdx.game.systems.RenderSystem;

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

        // use unit (i.e. 1.0f) for all dimensions - primitive objects will have scale applied by load()
        final float primUnit = 1.0f;
        final float primHE = 1f / 2f; // primitives half extent constant
        final float primCapsuleHt = 1.0f + 0.5f + 0.5f; // define capsule height ala bullet (HeightTotal = H + 1/2R + 1/2R)

        mb.begin();

        mb.node().id = "sphere";
        mb.part("sphere", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.GREEN))).sphere(1f, 1f, 1f, 10, 10);
//                new Material(ColorAttribute.createDiffuse(Color.GREEN), IntAttribute.createCullFace(GL_BACK))).sphere(1f, 1f, 1f, 10, 10);
        mb.node().id = "box";
        mb.part("box", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.BLUE))).box(1f, 1f, 1f);
        mb.node().id = "cone";
        mb.part("cone", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.YELLOW))).cone(1f, 1f, 1f, 10);
        mb.node().id = "capsule";
        mb.part("capsule", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.CYAN))).capsule(1f * primHE, primCapsuleHt, 10); // note radius and height vs. bullet
        mb.node().id = "cylinder";
        mb.part("cylinder", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.MAGENTA))).cylinder(1f, 1f, 1f, 10);

        primitivesModel = mb.end();

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
/* 
  Generate bullet shapes by applying the same scale/size as shall be applied to the vertices of the instance mesh. 
  primitive meshes should use unit value (1.0) for the extent dimensions, thus those base dimensions don't have to be multiplied in explicitly in the shape sizing calculation below.
  In some cases we have to take special care as bullet shapes don't all parameterize same way as gdx model primitives.
  Constant "primHE" (primitives-half-extent) is used interchangeably to compute radius from size.x, as well as half extents where needed.
 */
        sphereTemplate = new SizeableObject() {
            @Override
            public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                return load(model, rootNode, new btSphereShape(size.x * primHE), size, mass, trans);
            }
        };
        boxTemplate = new SizeableObject() {
            @Override
            public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                return load(model, rootNode, new btBoxShape(size.cpy().scl(primHE)), size, mass, trans);
            }
        };
        coneTemplate = new SizeableObject() {
            @Override
            public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                return load(model, rootNode, new btConeShape(size.x * primHE, size.y), size, mass, trans);
            }
        };
        capsuleTemplate = new SizeableObject() {
            @Override
            public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                // btcapsuleShape() takes actual radius parameter (unlike cone/cylinder which use width+depth)
                //  so we apply half extent factor to our size.x here.
                float radius = size.x * primHE;
                // btcapsuleShape total height is height+2*radius (unlike gdx capsule mesh where height specifies TOTAL height!
                //  (http://bulletphysics.org/Bullet/BulletFull/classbtCapsuleShape.html#details)
                // determine the equivalent bullet-compatible height parameter by explicitly scaling
                // the base mesh height and then subtracting the (scaled) end radii
                float height = primCapsuleHt * size.y - size.x * primHE - size.x * primHE;
                return load(model, rootNode, new btCapsuleShape(radius, height), size, mass, trans);
            }
        };
        cylinderTemplate = new SizeableObject() {
            @Override
            // cylinder shape apparently allow both width (x) and height (y) to be specified
            public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                return load(model, rootNode, new btCylinderShape(size.cpy().scl(primHE)), size, mass, trans);
            }
        };
    }


    private static void setObjectMaterial(ModelInstance inst, Color c, float alpha){

        Material mat = inst.materials.get(0);
        if (null == mat)
            return; // throw new GdxRuntimeException("not found");

        mat.set(ColorAttribute.createDiffuse(c));

        BlendingAttribute blendingAttribute =
                new BlendingAttribute(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, alpha);
        mat.set(blendingAttribute);
    }

private static int nextColor = 0;

    private static void setMaterialColor(Entity e, Color c){

        Array<Color> colors = new Array<Color>();
        colors.add(Color.WHITE);
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.YELLOW);
        colors.add(Color.PURPLE);

        // hmmm, w/ alt. pick test, now getting null somtimes?
        if (null == e){
            return; //  throw new GdxRuntimeException("e == null ");
        }

        ModelInstance inst = e.getComponent(ModelComponent.class).modelInst;

        Material mat = inst.materials.get(0);
        if (null == mat)
            return; // throw new GdxRuntimeException("not found");

        nextColor += 1;
        if (nextColor >= colors.size) {
            nextColor = 0;
        }

        ColorAttribute ca = (ColorAttribute) mat.get(ColorAttribute.Diffuse);

        for (Color color : colors) {
            if (ca.color != color) {
                mat.set(ColorAttribute.createDiffuse(color));
                break;
            }
        }

        setObjectMaterial(
                e.getComponent(ModelComponent.class).modelInst, colors.get(nextColor), 0.5f);
    }


    public static Entity pickObject;

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
                engine.addEntity(sphereTemplate.create(sphereTemplateModel,
                        null, size.x, translation, new Vector3(size.x, size.x, size.x)));
            }
        }


        Vector3 t = new Vector3(-10, +15f, -15f);
        Vector3 s = new Vector3(2, 3, 2); // scale (w, h, d, but usually should w==d )
        if (useTestObjects) {
            // assert (s.x == s.z) ... scaling of w & d dimensions should be equal
            addPickObject(engine, coneTemplate.create(primitivesModel, "cone", 5f, t, s));
            addPickObject(engine, capsuleTemplate.create(primitivesModel, "capsule", 5f, t, s));
            addPickObject(engine, cylinderTemplate.create(primitivesModel, "cylinder", 5f, t, s));
            pickObject =
                    addPickObject(engine, boxTemplate.create(primitivesModel, "box", 5f, t, s));

            ModelComponent tmp = pickObject.getComponent(ModelComponent.class);
            tmp.id = 65535;
        }


        Entity skybox = GameObject.load(sceneModel, "space");
        skybox.getComponent(ModelComponent.class).isShadowed = false; // disable shadowing of skybox
        engine.addEntity(skybox);


        if (true) { // this slows down bullet debug drawer considerably!
            Entity ls = GameObject.load(landscapeModel);
            engine.addEntity(ls);

            // put the landscape at an angle so stuff falls of it...
            final float yTrans = -10.0f;
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
        player = GameObject.load(model, node, null, 5.1f, new Vector3(-1, 11f, -5f), boxshape);
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

        Entity e = boxTemplate.create(primitivesModel, "box",0,
                new Vector3(0, 10, -5), new Vector3(4f, 1f, 4f));
        setObjectMaterial(
                e.getComponent(ModelComponent.class).modelInst, Color.CHARTREUSE, 0.5f);
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

        float r = 0.5f;
        Entity e = GameObject.load(primitivesModel, "sphere", new Vector3(r, r, r), new Vector3(0, 15f, -5f));

        ModelComponent mc = e.getComponent(ModelComponent.class);

        mc.modelInst.userData = 0xaa55;
        e.add(new CharacterComponent(
                new PIDcontrol(tgtTransform, mc.modelInst.transform, new Vector3(0, 2, 3), 0.1f, 0, 0)));

        engine.addEntity(e);
        return e;
    }

/*
 * extended objects ... a bullet object, optionally a "kinematic" body.
 * primitive object templates have an appropriately sized basic bullet shape bound to them automatically
 * by the overloaded create() method.
 * You would want to be able set a custom material color/texture on them.
 * if same shape instance to be used for multiple entities, then shape would need to be instanced only
 * once in the constructor so you would need to do it a different way/ ???? investigate?.
 * Bounding box only works on mesh and doesn't work if we are scaling the mesh :(
 */
public static class SizeableObject extends GameObject {

    // needed for method override (make this class from an interface, derived GameObject?)
    // can't override declare static method in anonymous inner class
    public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
        return null;
    }

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
//  Disposing the model will automatically make all instances invalid!
        sceneModel.dispose();
        sphereTemplateModel.dispose();
        boxTemplateModel.dispose();
        primitivesModel.dispose();
        assets.dispose();
    }

    // tmp tmp hack hack
    private static final Array<Entity> pickObjects = new Array<Entity>();

    private static Entity addPickObject(Engine engine, Entity e) {
        engine.addEntity(e);
        pickObjects.add(e);
        return e; // for method call chaining
    }

    private static Vector3 position = new Vector3();

    /*
     * https://xoppa.github.io/blog/interacting-with-3d-objects/
     */
    public void applyPickRay(Ray ray) {

        Entity picked = null;
        float distance = -1f;

        for (Entity e : pickObjects) {

            ModelComponent mc = e.getComponent(ModelComponent.class);

            mc.modelInst.transform.getTranslation(position).add(mc.center);

            if (mc.id == 65535) {
                RenderSystem.testRayLine = RenderSystem.lineTo(ray.origin, position, Color.LIME);
            }

if (false) {
    float dist2 = ray.origin.dst2(position);

    if (distance >= 0f && dist2 > distance)
        continue;

    if (Intersector.intersectRaySphere(ray, position, mc.boundingRadius, null)) {
        picked = e;
        distance = dist2;
    }
}else{
    final float len = ray.direction.dot(
            position.x - ray.origin.x,
            position.y - ray.origin.y,
            position.z - ray.origin.z);

    if (len < 0f)
        continue;

    float dist2 = position.dst2(
            ray.origin.x + ray.direction.x * len,
            ray.origin.y + ray.direction.y * len,
            ray.origin.z + ray.direction.z * len);

    if (distance >= 0f && dist2 > distance)
        continue;

    if (dist2 <= mc.boundingRadius * mc.boundingRadius) {
        picked = e;
        distance = dist2;
    }
}
            /*            Gdx.app.log("asdf", String.format("mc.id=%d, dx = %f, pos=(%f,%f,%f)",
                    mc.id, distance, position.x, position.y, position.z ));*/
        }
        setMaterialColor(picked, Color.RED);
    }
}
