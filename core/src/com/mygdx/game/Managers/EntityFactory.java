package com.mygdx.game.Managers;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.screens.physObj;

import java.util.Random;


/**
 * Created by neiderm on 12/21/2017.
 */

public class EntityFactory {

    static private Model boxTemplateModel;
    static private Model ballTemplateModel;

    static {
        final ModelBuilder modelBuilder = new ModelBuilder();

        Texture cubeTex = new Texture(Gdx.files.internal("data/crate.png"), false);
        boxTemplateModel  = modelBuilder.createBox(2f, 2f, 2f,
                new Material(TextureAttribute.createDiffuse(cubeTex)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        Texture sphereTex = new Texture(Gdx.files.internal("data/day.png"), false);
        ballTemplateModel = modelBuilder.createSphere(2f, 2f, 2f, 16, 16,
                new Material(TextureAttribute.createDiffuse(sphereTex)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
    }


    static private final int N_ENTITIES = 21;
    static private final int N_BOXES = 10;


    public enum pType {
        SPHERE, BOX
    }



    static private void CreateObject(BulletComponent bc, ModelComponent mc,
                                     pType tp, Vector3 sz, float mass, Matrix4 transform) {

        ModelInstance modelInst = null;

        if (tp == pType.BOX) {
            bc.shape = new btBoxShape(sz);
            modelInst = new ModelInstance(boxTemplateModel);
        }

        if (tp == pType.SPHERE) {
            sz.y = sz.x;
            sz.z = sz.x; // sphere must be symetrical!
            bc.shape = new btSphereShape(sz.x);
            modelInst = new ModelInstance(ballTemplateModel);
        }

        modelInst.transform = new Matrix4(transform);

//        mc.modelInst  = modelInst;
        bc.modelInst = modelInst;
        bc.scale = new Vector3(sz);


        Vector3 tmp = new Vector3();

        if (mass == 0) {
            modelInst.transform.scl(sz);
            tmp = Vector3.Zero.cpy();
            bc.motionstate = null;
        } else {
            bc.shape.calculateLocalInertia(mass, tmp);
            bc.motionstate = new BulletComponent.MotionState(modelInst.transform);
        }

        btRigidBody.btRigidBodyConstructionInfo bodyInfo ;
        bodyInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, bc.motionstate, bc.shape, tmp);
        bc.body = new btRigidBody(bodyInfo);
        bc.body.setFriction(0.8f);

        bodyInfo.dispose();

        if (mass == 0) {
            bc.body.translate(tmp.set(modelInst.transform.val[12], modelInst.transform.val[13], modelInst.transform.val[14]));
        }
        physObj pob = new physObj(bc.body);
    }

    static public Entity CreateEntity(
            Engine engine, pType tp, Vector3 sz, float mass, Matrix4 transform) {

        Entity e = new Entity();
        engine.addEntity(e);

        ModelComponent mc = new ModelComponent();
        e.add(mc);

        BulletComponent bc = new BulletComponent();
        e.add(bc);

        CreateObject(bc, mc, tp, sz, mass, transform);

        return e;
    }

    static public void CreateEntities(Engine engine /*, AssetManager assets */) {

        Vector3 tmpV = new Vector3(); // size
        Matrix4 tmpM = new Matrix4(); // transform
        Random rnd = new Random();

        for (int i = 0; i < N_ENTITIES; i++) {
            tmpV.set(rnd.nextFloat() + .1f, rnd.nextFloat() + .1f, rnd.nextFloat() + .1f);
            tmpM.idt().trn(rnd.nextFloat() * 10.0f - 5f, rnd.nextFloat() + 25f, rnd.nextFloat() * 10.0f - 5f);

            pType tp = pType.BOX;

            if (i >= N_BOXES) {
                tp = pType.SPHERE;
            }

            CreateEntity(engine, tp, tmpV, rnd.nextFloat() + 0.5f, tmpM);
        }


        // uncomment for a terrain alternative;
        //tmpM.idt().trn(0, -4, 0);
        //new physObj(physObj.pType.BOX, tmpV.set(20f, 1f, 20f), 0, tmpM);	// zero mass = static
        tmpM.idt().trn(10, -5, 0);
        EntityFactory.CreateEntity(engine, EntityFactory.pType.SPHERE, tmpV.set(8f, 8f, 8f), 0, tmpM);
    }


    static public void dispose(){

        boxTemplateModel.dispose();
        ballTemplateModel.dispose();
    }
}
