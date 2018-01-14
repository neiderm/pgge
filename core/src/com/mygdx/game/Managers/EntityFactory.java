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
     * currently there is no instance variables in this class.
     *  everything done in create() so that it can return an Entity, which conceivable could be
     * handed off to an instance of an appropriate factory.
     * Might want to think about what instance variable could allow
     * instances of objects that can be reused/pooled
     */
    private abstract static class GameObject {

        Entity create() {
            Entity e = new Entity();
            return e;
        }

        Entity create(Model model, Float mass, Matrix4 transform, Vector3 size, btCollisionShape shape) {

            Entity e = create();

            // really? this will be bullet comp motion state linked to same copy of instance transform?
//        Matrix4 crap = transform;
            Matrix4 crap = new Matrix4(transform); // defensive copy, must NOT assume caller made a new instance!

            e.add(new ModelComponent(model, crap, size)); // model is STATIC, not instance vairable!!!!!
            e.add(new BulletComponent(shape, crap, mass));

            return e;
        }
    }

    private static class SphereObject extends GameObject {

        Entity create(/* Model model, */ float mass, float radius, Matrix4 trans) {

            Vector3 size = new Vector3(radius, radius, radius);
            Entity e = create(ballTemplateModel, mass, trans, size, new btSphereShape(radius));
            return e;
        }
    }

    private static class BoxObject extends GameObject {

        Entity create(/* Model model, */ float mass, Vector3 size, Matrix4 trans){

            Entity e = create(boxTemplateModel, mass, trans, size, new btBoxShape(size));
            return e;
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
     * static things that are on the landscape ... we might want lots of these
     */
    private static class ThingObject extends GameObject {
    }


    /*
     derived factories do special sauce for static vs dynamic entities:
     */
    private abstract class ObjectFactory<T extends GameObject>{

        T object;
//        ObjectFactory(){;}
        ObjectFactory(T object) {this.object = object;}
        Entity create() {
             return object.create();
        }
    }
    private class StaticObjectFactory extends ObjectFactory<ThingObject >{

        StaticObjectFactory(ThingObject object) {
           super(object);
        }

        @Override
        Entity create() {
            return (super.create());
        }
    }

    private void makeObjects() {
        ThingObject object = new ThingObject ();
        StaticObjectFactory factory = new StaticObjectFactory(object);
        makeEntities(factory);
    }

    private void makeEntities(ObjectFactory factory){
        factory.create();
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
        Matrix4 tmpM = new Matrix4(); // transform
        Random rnd = new Random();

        for (int i = 0; i < N_ENTITIES; i++) {
            tmpV.set(rnd.nextFloat() + .1f, rnd.nextFloat() + .1f, rnd.nextFloat() + .1f);
            tmpM.idt().trn(rnd.nextFloat() * 10.0f - 5f, rnd.nextFloat() + 25f, rnd.nextFloat() * 10.0f - 5f);

            if (i < N_BOXES) {
                engine.addEntity(new BoxObject().create(rnd.nextFloat() + 0.5f, tmpV, tmpM));
            } else {
                engine.addEntity(new SphereObject().create(rnd.nextFloat() + 0.5f, tmpV.x, tmpM));
            }
        }

        Entity e;

        tmpM.idt().trn(0, -4, 0);
        e = new BoxObject().create(0f, tmpV.set(20f, 1f, 20f), tmpM);
        engine.addEntity(e);
        createEntity(e);

        tmpM.idt().trn(10, 5, 0);
        e = new SphereObject().create(0f, 8, tmpM);
        engine.addEntity(e);
        createEntity(e);

//        createGround(engine);

        // put the landscape at an angle so stuff falls of it...
        Matrix4 transform = new Matrix4().idt().rotate(new Vector3(1, 0, 0), 20f);
        engine.addEntity(new LandscapeObject().create(landscapeModel, transform));
    }

    private static void createGround(Engine engine){

        Entity e = new Entity();
        engine.addEntity(e);

        Vector3 size = new Vector3(20, 1, 20);

        Matrix4 transform = new Matrix4().idt().trn(0, -4, 0);

//        createEntity(engine, new BoxObject(new Vector3(20f, 1f, 20f), transform));	// zero mass = static
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
        mc.modelInst.transform.scl(size);
    }


    public static void dispose(){

        boxTemplateModel.dispose();
        ballTemplateModel.dispose();
        model.dispose();
        assets.dispose();
    }
}
