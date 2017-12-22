package com.mygdx.game.Managers;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.screens.physObj;

import java.util.Random;


/**
 * Created by neiderm on 12/21/2017.
 */

public class EntityFactory {

    static final int N_ENTITIES = 300;
    static final int N_BOXES = 200;

    public static Model boxTemplateModel;
    public static Model ballTemplateModel;

    public enum pType {
        SPHERE, BOX
    };

    static private physObj CreateObject(
            pType tp, Vector3 sz, float mass, Matrix4 transform, ModelInstance modelInst) {

        btCollisionShape shape = null;

        if (tp == pType.BOX) {
            shape = new btBoxShape(sz);
            modelInst = new ModelInstance(boxTemplateModel);
        }

        if (tp == pType.SPHERE) {
            sz.y = sz.x;
            sz.z = sz.x; // sphere must be symetrical!
            shape = new btSphereShape(sz.x);
            modelInst = new ModelInstance(ballTemplateModel);
        }

        modelInst.transform = transform.cpy(); // probably ok not to cpy here ;)


        physObj pob = new physObj(sz, mass, modelInst, shape);

        return pob;
    }

    static public Entity CreateEntity(
            Engine engine, pType tp, Vector3 sz, float mass, Matrix4 transform){

        ModelComponent mc = new ModelComponent();

        physObj pob = CreateObject(tp, sz, mass, transform, mc.modelInst);


        Entity e = new Entity();
        engine.addEntity(e);

// tmp: pob could be created in bc constructor, but this is temporary ;)
        BulletComponent bc = new BulletComponent();

        bc.pob = pob;
        e.add(bc);

        return e;
    }

    static public void CreateEntities(Engine engine /*, AssetManager assets */) {

        Vector3 tmpV = new Vector3(); // size
        Matrix4 tmpM = new Matrix4(); // transform
        Random rnd = new Random();

        for (int i = 0; i < N_ENTITIES; i++) {
            tmpV.set(rnd.nextFloat() + .1f, rnd.nextFloat() + .1f, rnd.nextFloat() + .1f);
            tmpM.idt().trn(rnd.nextFloat() * 10.0f - 5f, rnd.nextFloat() + 25f, rnd.nextFloat() * 10.0f - 5f);

            pType tp;
            tp = pType.BOX;

            if (i > N_BOXES) {
                tp = pType.SPHERE;
            }

            // probabbly not necessary to cpy
            CreateEntity(engine, tp, tmpV.cpy(), rnd.nextFloat() + 0.5f, tmpM);
        }
    }
}
