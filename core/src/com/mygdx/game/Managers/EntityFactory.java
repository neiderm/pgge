package com.mygdx.game.Managers;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.screens.physObj;

import java.util.Random;

/**
 * Created by neiderm on 12/21/2017.
 */

public class EntityFactory {

    static final int N_ENTITIES = 300;
    static final int N_BOXES = 200;


    static public physObj CreateObject(physObj.pType tp, Vector3 sz, float mass, Matrix4 transform) {

        physObj pob = new physObj(tp, sz, mass, transform);
        return pob;
    }

    static public Entity CreateEntity(
            Engine engine, physObj.pType tp, Vector3 sz, float mass, Matrix4 transform){

        physObj pob = CreateObject(tp, sz, mass, transform);

        Entity e = new Entity();
        engine.addEntity(e);

// tmp: pob could be created in bc constructor, but this is temporary ;)
        BulletComponent bc = new BulletComponent();

        bc.pob = pob;
        e.add(bc);

        return e;
    }

    static public void CreateEntities(Engine engine /*, AssetManager assets */) {

        Vector3 tmpV = new Vector3();
        Matrix4 tmpM = new Matrix4();
        Random rnd = new Random();

        physObj pob;

        for (int i = 0; i < N_ENTITIES; i++) {
            tmpV.set(rnd.nextFloat() + .1f, rnd.nextFloat() + .1f, rnd.nextFloat() + .1f);
            tmpM.idt().trn(rnd.nextFloat() * 10.0f - 5f, rnd.nextFloat() + 25f, rnd.nextFloat() * 10.0f - 5f);

            physObj.pType tp;
            tp = physObj.pType.BOX;

            if (i > N_BOXES) {
                tp = physObj.pType.SPHERE;
            }

            CreateEntity(engine, tp, tmpV.cpy(), rnd.nextFloat() + 0.5f, tmpM);
        }
    }
}
