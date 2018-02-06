package com.mygdx.game.screens;

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
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.CameraComponent;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.Components.PlayerComponent;
import com.mygdx.game.Managers.EntityFactory.BoxObject;
import com.mygdx.game.Managers.EntityFactory.GameObject;
import com.mygdx.game.Managers.EntityFactory.LandscapeObject;
import com.mygdx.game.Managers.EntityFactory.SphereObject;
import com.mygdx.game.Managers.EntityFactory.StaticEntiteeFactory;

import java.util.Random;

/**
 * Created by mango on 12/18/17.
 */

public class physObj {

    private static boolean useTestObjects = true;
    private static Model model;
    private static final AssetManager assets;
    private static final Model landscapeModel;
    private static final Model shipModel;
    private static Model boxTemplateModel;
    private static Model sphereTemplateModel;
    private static Model ballTemplateModel;
    private static Model tankTemplateModel;

    private physObj(){
    }

    static Vector3 tankSize = new Vector3(1, 1, 2);

    static {
        final ModelBuilder mb = new ModelBuilder();

        Texture cubeTex = new Texture(Gdx.files.internal("data/crate.png"), false);
        boxTemplateModel  = mb.createBox(1f, 1f, 1f,
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

        tankTemplateModel  = mb.createBox(tankSize.x, tankSize.y, tankSize.z,
                new Material(TextureAttribute.createDiffuse(ballTex)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        assets = new AssetManager();
        assets.load("data/landscape.g3db", Model.class);
        assets.load("data/ship.g3db", Model.class);
        assets.finishLoading();
        landscapeModel = assets.get("data/landscape.g3db", Model.class);
        shipModel = assets.get("data/ship.g3db", Model.class);

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

        model = mb.end();
    }


    public static void createEntities(Engine engine) {

        final int N_ENTITIES = 1;
        final int N_BOXES = 10;

        Vector3 tmpV = new Vector3(); // size
        Random rnd = new Random();

        for (int i = 0; i < N_ENTITIES; i++) {

            tmpV.set(rnd.nextFloat() + .1f, rnd.nextFloat() + .1f, rnd.nextFloat() + .1f);
            tmpV.scl(2.0f); // this keeps object "same" size relative to previous model size was 2x

            Vector3 translation =
                    new Vector3 (rnd.nextFloat() * 10.0f - 5f, rnd.nextFloat() + 25f, rnd.nextFloat() * 10.0f - 5f);

            if (i < N_BOXES) {
                engine.addEntity(
                        new BoxObject(tmpV, boxTemplateModel).create(tmpV.x, translation ));
            } else {
                engine.addEntity(
                        new SphereObject(tmpV.x, sphereTemplateModel).create(tmpV.x, translation ));
            }
        }


        Vector3 t = new Vector3(0, 0 + 25f, 0 - 5f);
        Vector3 s = new Vector3(1, 1, 1);
if (useTestObjects) {
    engine.addEntity(
            new GameObject(s, model, "cone").create(rnd.nextFloat() + 0.5f, t,
                    new btConeShape(0.5f, 2.0f)));

    engine.addEntity(
            new GameObject(s, model, "capsule").create(rnd.nextFloat() + 0.5f, t,
                    new btCapsuleShape(0.5f, 0.5f * 2.0f)));

    engine.addEntity(
            new GameObject(s, model, "cylinder").create(rnd.nextFloat() + 0.5f, t,
                    new btCylinderShape(new Vector3(0.5f * 1.0f, 0.5f * 2.0f, 0.5f * 1.0f))));
} //


if (useTestObjects) {
    BoxObject bo = new BoxObject(new Vector3(2, 2, 2), boxTemplateModel);

    engine.addEntity(bo.create(0.1f, new Vector3(0, 0 + 4, 0 - 5f)));
    engine.addEntity(bo.create(0.1f, new Vector3(-2, 0 + 4, 0 - 5f)));
    engine.addEntity(bo.create(0.1f, new Vector3(-4, 0 + 4, 0 - 5f)));
    engine.addEntity(bo.create(0.1f, new Vector3(0, 0 + 6, 0 - 5f)));
    engine.addEntity(bo.create(0.1f, new Vector3(-2, 0 + 6, 0 - 5f)));
    engine.addEntity(bo.create(0.1f, new Vector3(-4, 0 + 6, 0 - 5f)));
} //
        int wallW = 0;
        int wallH = 0;
        for (wallH = 0; wallH < 2; wallH++) {
            for (wallW = 0; wallW < 3; wallW++) {

            }
        }


        
        StaticEntiteeFactory<GameObject> staticFactory =
                new StaticEntiteeFactory<GameObject>();

        float yTrans = -10.0f;
        Vector3 tran = new Vector3(0, -4 + yTrans, 0);

        engine.addEntity( staticFactory.create(
                new BoxObject(new Vector3(40f, 2f, 40f), boxTemplateModel), tran) );

        engine.addEntity( staticFactory.create(
                new SphereObject(16, sphereTemplateModel), new Vector3(10, 5 + yTrans, 0)) );

        engine.addEntity( staticFactory.create(
                new BoxObject(new Vector3(40f, 2f, 40f), model, "box"), new Vector3(-15, 1, -20) ) );

if (false) {
    // put the landscape at an angle so stuff falls of it...
// TODO: ship is "sticking" to landscape
    Matrix4 transform = new Matrix4().idt().rotate(new Vector3(1, 0, 0), 20f);
    transform.trn(0, 0 + yTrans, 0);
    engine.addEntity(new LandscapeObject().create(landscapeModel, transform));
}


// TODO: intatiate object as dynamic, let it fall, then let it rest as static (take out of dynamics world)


        // visual marker for camera object
        Entity e =
                new GameObject(new Vector3(0.25f, 0.5f, 0.6f), model, "cone").create(new Vector3(0, 15f, -5f));
        // static entity not use motion state so just set the scale on it once and for all
        ModelComponent mc = e.getComponent(ModelComponent.class);
        mc.modelInst.transform.scl(mc.scale);
        e.add(new CameraComponent());
        engine.addEntity(e);
    }


    public static Entity createPlayer(Engine engine  )
    {
        Vector3 s = new Vector3(1, 1, 1);

        /*
        float mass = 1.0f;
        Entity plyr = new GameObject(s, tankTemplateModel).create(
                mass, t, new btBoxShape(tankSize.cpy().scl(0.5f)));
        plyr.add(new PlayerComponent(mass));
    */
        float mass = 5.1f; // can't go much more mass, ball way too fast!
//        Entity plyr = new GameObject(s, ballTemplateModel).create(
        Entity plyr = new GameObject(s, shipModel).create(
                mass, new Vector3(0, 15f, -5f),
                new btBoxShape(new Vector3(0.5f, 0.35f, 0.75f)));
//                new btConeShape(0.75f, 0.25f));
//                new btCylinderShape(new Vector3(0.5f, 0.25f, 0.75f)));
        plyr.add(new PlayerComponent(mass));
        engine.addEntity(plyr);

//        Matrix4 tmpM = new Matrix4();
//        btRigidBody body = plyr.getComponent(BulletComponent.class).body;
//        body.getWorldTransform(tmpM);

        // these rotations are equivalent!!!
//        tmpM.rotate(1, 0, 0, -90);
//        tmpM.rotate(-1, 0, 0, 90);

        //        tmpM.getTranslation(tmpV);
//                tmpM.setFromEulerAngles(0, -90, 0);  // but this one clears translation!
//        tmpM.setTranslation(tmpV.x, tmpV.y, tmpV.z);

//        body.setWorldTransform(tmpM); // setCenterOfMassTransform

        return plyr;
    }


    public static void dispose(){

        // The Model owns the meshes and textures, to dispose of these, the Model has to be disposed. Therefor, the Model must outlive all its ModelInstances
//  Disposing the model will automatically make all instances invalid!
        tankTemplateModel.dispose();
        sphereTemplateModel.dispose();
        boxTemplateModel.dispose();
        ballTemplateModel.dispose();
        model.dispose();
        assets.dispose();
    }

}
