package com.mygdx.game.Managers;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.ModelComponent;

import java.util.Random;


/**
 * Created by neiderm on 12/21/2017.
 */

public class EntityFactory {

    private static Model model;
    private static final AssetManager assets;
    private static final Model landscapeModel;

    private enum pType {
        SPHERE, BOX
    }

    private static Model boxTemplateModel;
    private static Model ballTemplateModel;

    static {
        final ModelBuilder mb = new ModelBuilder();

        Texture cubeTex = new Texture(Gdx.files.internal("data/crate.png"), false);
        boxTemplateModel  = mb.createBox(2f, 2f, 2f,
                new Material(TextureAttribute.createDiffuse(cubeTex)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        Texture sphereTex = new Texture(Gdx.files.internal("data/day.png"), false);
        ballTemplateModel = mb.createSphere(2f, 2f, 2f, 16, 16,
                new Material(TextureAttribute.createDiffuse(sphereTex)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        assets = new AssetManager();
        assets.load("data/landscape.g3db", Model.class);
        assets.finishLoading();
        landscapeModel = assets.get("data/landscape.g3db", Model.class);


        final float groundW = 25.0f;
        final float groundH = 1.0f;
        final float groundD = 25.0f;

        mb.begin();

        mb.node().id = "ground";
        mb.part("ground", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.RED))).box(groundW, groundH, groundD);
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

        model = mb.end();
    }


    /*
     */
    private abstract static class GameObject {

        protected Model model;
        protected Vector3 size;

        GameObject() {}

        GameObject(Model model, Vector3 size) {
            this.model = model;
            this.size = size;
        }

        Entity create() {
            return new Entity();
        }

        Entity create(float mass, Vector3 translation){
            return new Entity();
        }

        Entity create(float mass, Vector3 translation, btCollisionShape shape) {

            Entity e = create();

            // really? this will be bullet comp motion state linked to same copy of instance transform?
            // defensive copy, must NOT assume caller made a new instance!
            Matrix4 transform = new Matrix4().idt().trn(translation);

            e.add(new ModelComponent(model, transform, size));
            e.add(new BulletComponent(shape, transform, mass));

            return e;
        }
    }

    private static class SphereObject extends GameObject {

        private float radius;

        SphereObject(float radius) {
            super(ballTemplateModel, new Vector3(radius, radius, radius));
            this.radius = radius;
        }

        Entity create(/* Model model, */ float mass, Vector3 translation) {

            return super.create(mass, translation, new btSphereShape(radius));
        }
    }

    private static class BoxObject extends GameObject {

        BoxObject(Vector3 size) {
            this(boxTemplateModel, size);
        }

        BoxObject(Model model, Vector3 size) {
            super(model, size);
        }

        Entity create(/* Model model, */ float mass, Vector3 translation){

            return super.create(mass, translation, new btBoxShape(size));
        }
    }

    /*
     * we might want lots of these ... islands in the sky, all made of mesh shapes
     */
    private static class LandscapeObject extends GameObject {

//         Entity create(final Array<T> meshParts, Matrix4 transform){ ??????
        Entity create(Model model, Matrix4 transform){

            Entity e = create();

            e.add(new BulletComponent(
                    new btBvhTriangleMeshShape(model.meshParts), transform));

            e.add(new ModelComponent(model, transform));

            return e;
        }
    }


    /*
     derived factories do special sauce for static vs dynamic entities:
     */
    private static /* abstract */ class EntiteeFactory<T extends GameObject>{

//        T object;

        EntiteeFactory(){}
/*
        EntiteeFactory(T object){
            this.object = object;
        }
*/
//        Entity create() {
//            return create(0, new Vector3(0, 0, 0));
//        }

        Entity create(T object, float mass, Vector3 translation) {
            return object.create(mass, translation);
        }
    }

    private static class StaticEntiteeFactory<T extends GameObject> extends EntiteeFactory{
/*
        StaticEntiteeFactory(T object){
            super(object);
        }
*/
        Entity create(T object, Vector3 translation) {
            Entity e = object.create(0f, translation);

            // special sauce here for static entity
            Vector3 tmp = new Vector3();
            BulletComponent bc = e.getComponent(BulletComponent.class);
            ModelComponent mc = e.getComponent(ModelComponent.class);

            // bc.body.translate(tmp.set(modelInst.transform.val[12], modelInst.transform.val[13], modelInst.transform.val[14]));
            bc.body.translate(mc.modelInst.transform.getTranslation(tmp));

            // static entity not use motion state so just set the scale on it once and for all
            mc.modelInst.transform.scl(mc.scale);

            return e;
        }
    }


    // static entity (tmp, will be done in factory or in a game object derived for static?)
    private static Entity createEntity(Entity e){

//        float mass = 0f;
//        Entity e = createEntity(engine, object, mass);

        // special sauce here for static entity
        Vector3 tmp = new Vector3();
        BulletComponent bc = e.getComponent(BulletComponent.class);
        ModelComponent mc = e.getComponent(ModelComponent.class);

        // bc.body.translate(tmp.set(modelInst.transform.val[12], modelInst.transform.val[13], modelInst.transform.val[14]));
        bc.body.translate(mc.modelInst.transform.getTranslation(tmp));

        // static entity not use motion state so just set the scale on it once and for all
//        mc.modelInst.transform.scl(object.size);
        mc.modelInst.transform.scl(mc.scale);

        return e;
    }


    public static void createEntities(Engine engine) {

        final int N_ENTITIES = 21;
        final int N_BOXES = 10;

        Vector3 tmpV = new Vector3(); // size
        Random rnd = new Random();

        for (int i = 0; i < N_ENTITIES; i++) {

            tmpV.set(rnd.nextFloat() + .1f, rnd.nextFloat() + .1f, rnd.nextFloat() + .1f);

            Vector3 translation =
                    new Vector3 (rnd.nextFloat() * 10.0f - 5f, rnd.nextFloat() + 25f, rnd.nextFloat() * 10.0f - 5f);

            if (i < N_BOXES) {
                engine.addEntity(new BoxObject(tmpV).create(rnd.nextFloat() + 0.5f, translation ));
            } else {
                engine.addEntity(new SphereObject(tmpV.x).create(rnd.nextFloat() + 0.5f, translation ));
            }
        }

        Entity e;
        float yTrans = -10.0f;
        Vector3 tran = new Vector3(0, -4 + yTrans, 0);
        BoxObject bo = new BoxObject(boxTemplateModel, new Vector3(20f, 1f, 20f));
        EntiteeFactory<BoxObject> bigCrateFactory = new EntiteeFactory<BoxObject>();

        if (false)
            e = bo.create(0, tran);
        else {
            e = bigCrateFactory.create(bo, 0, tran);
        }
        engine.addEntity(e);
        createEntity(e);


        StaticEntiteeFactory<SphereObject> bigSphereFactory =
                new StaticEntiteeFactory<SphereObject>();
        engine.addEntity( bigSphereFactory.create(
                new SphereObject(8), new Vector3(10, 5 + yTrans, 0)) );


        createGround(engine);




        // put the landscape at an angle so stuff falls of it...
        Matrix4 transform = new Matrix4().idt().rotate(new Vector3(1, 0, 0), 20f);
        transform.trn(0, 0 + yTrans, 0);
        engine.addEntity(new LandscapeObject().create(landscapeModel, transform));
    }

    private static void createGround(Engine engine){

        Entity e = new Entity();
        engine.addEntity(e);

        Vector3 size = new Vector3(20, 1, 20);

        Matrix4 transform = new Matrix4().idt().trn(-15, 1, -20);

        btBoxShape shape = new btBoxShape(size);
        e.add(new BulletComponent(shape, transform, 0.0f));

        e.add(new ModelComponent(model, transform, "box"));


        // special sauce here for static entity
        Vector3 tmp = new Vector3();
        BulletComponent bc = e.getComponent(BulletComponent.class);
        ModelComponent mc = e.getComponent(ModelComponent.class);

        // bc.body.translate(tmp.set(modelInst.transform.val[12], modelInst.transform.val[13], modelInst.transform.val[14]));
        bc.body.translate(mc.modelInst.transform.getTranslation(tmp));

        // static entity not use motion state so just set the scale on it once and for all
        mc.modelInst.transform.scl(size.scl(2.0f)); // hack hack hack, we're not creating collision shapes w/ half extents!
    }


    public static void dispose(){

        boxTemplateModel.dispose();
        ballTemplateModel.dispose();
        model.dispose();
        assets.dispose();
    }
}
