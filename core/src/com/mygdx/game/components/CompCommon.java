/*
 * Copyright (c) 2019 Glenn Neidermeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mygdx.game.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.GameWorld;
import com.mygdx.game.features.BurnOut;
import com.mygdx.game.features.FeatureAdaptor;
import com.mygdx.game.sceneLoader.GameObject;
import com.mygdx.game.sceneLoader.InstanceData;
import com.mygdx.game.util.PrimitivesBuilder;

/*
 * the catch-all of the moment ...
 */
public class CompCommon {

    CompCommon() { // mt
    }

    public enum ImpactType {
        FATAL,
        DAMAGING,
        ACQUIRE
    }

    /*
         note: caller decides if/how to dispose of its target
     this is still wonky and need generalized more

     can designate what hierarchy "level" is the root node at which to load?
     kludgey  loading being done for exploding model
     */
    public static void exploducopia(ModelInstance modelInst, String targetMdlInfoKey) {
//        if (null != targetMdlInfoKey)
        {
            Vector3 translation = new Vector3();
            modelInst.transform.getTranslation(translation);

            Quaternion rotation = new Quaternion();
            rotation = modelInst.transform.getRotation(rotation);

// note * .... need to inform the level !!!!!!
            spawnNewGameObject(
                    translation, rotation, "*", targetMdlInfoKey);
        }
    }

    /*
    The thing that is going 'gaBoom' should be able to specify Material texture,  Color Attr. only)
    (or else if no Texture Attrib. then we assign a default (fire-y!!) one! ?

     IN: points : because floating signboarded  points
    */
    public static void makeBurnOut(ModelInstance mi, ImpactType useFlags) {

        mi.userData = null; //  null forces default color

        if (ImpactType.ACQUIRE == useFlags) { // marker for prize pickup
            mi.userData = new Color(Color.SKY); // hacky hackhackster
        } else if (ImpactType.DAMAGING == useFlags) { // marker for hit/collision w/ damage
            mi.userData = new Color(Color.YELLOW);
        }

        Vector3 translation = new Vector3(); // tmp for new vector instance .. only need to feed the GC relavitvely few of thsesei guess

        InstanceData id = new InstanceData(mi.transform.getTranslation(translation));

        id.adaptr = new BurnOut(mi); // there it is

        final String tmpObjectName = "sphere";
        GameObject gameObject = new GameObject(tmpObjectName);
        gameObject.getInstanceData().add(id);

        // insert a newly created game OBject into the "spawning" model group
        GameWorld.getInstance().addSpawner(gameObject);
    }

    /*
     * Object creator for dynamic spawning
     * Caller can specify the Feature Adapter, mesh shape and material to use, (or let defaults) thus
     * allowing the  type of explosion or whatever effect to be per-caller.
     * Game Object is made but not the entity yet, as delayed-queued for spawnning.
     */
    private static void spawnNewGameObject(
            Vector3 translation, Quaternion rotation,
            String objectName, String modelInfoKey) {

        // insert a newly created game OBject into the "spawning" model group
        GameObject gameObject = new GameObject(objectName);

// fixed for now ;
        gameObject.mass = 1f;
        gameObject.meshShape = "convexHullShape";

        InstanceData id = new InstanceData(translation);

        id.rotation = new Quaternion();// tmp should not need to new it here!
        id.rotation.set(rotation);

        //        gameObject.mass = 1; // let it be stationary
        gameObject.getInstanceData().add(id);

// any sense for Game Object have its own static addspawner() method ?  (need the Game World import/reference here ?)
        GameWorld.getInstance().addSpawner(gameObject, modelInfoKey); //  is added "kinematic" ???
    }

    public static void spawnNewGameObject(
            Vector3 scale, Vector3 translation, FeatureAdaptor fa, String objectName) {

        InstanceData id = new InstanceData(translation);
//        if (null != fa)
        {
            id.adaptr = fa;
        }

        // insert a newly created game OBject into the "spawning" model group
        GameObject gameObject = new GameObject(objectName);
        gameObject.scale = scale;

        gameObject.getInstanceData().add(id);

// any sense for Game Object have its own static addspawner() method ?  (need the Game World import/reference here ?)
        GameWorld.getInstance().addSpawner(gameObject); // toooodllly dooodddd    object is added "kinematic" ???
    }


    /*
     * doesn't do much more than flag the comp for removal
     * set the collision flags is probably pointless
     */
    public static void physicsBodyMarkForRemoval(Entity ee) {

        BulletComponent bc = ee.getComponent(BulletComponent.class);
        if (null == bc) {
            Gdx.app.log("collisionHdlr", "BulletComponent bc =  === NULLLL");
            return; // bah processing object that should already be "at rest" ???? .....................................................
        }

        bc.body.setCollisionFlags(
                bc.body.getCollisionFlags() & ~btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);

        StatusComponent sc = new StatusComponent();
        sc.lifeClock = 9999;
        sc.deleteFlag = 2;         // flag bullet Comp for deletion
        ee.add(sc);
    }

    /*
     * dynamically "activate" a template entity and set its location
     *  creates a graphical mesh-shape and a matching physics body, adds it to the bullet world
     */
    public static void entityAddPhysicsBody(Entity ee, Vector3 translation) {


        // tooooo dooo how to handle shape?
        btCollisionShape shape = PrimitivesBuilder.getShape("box", new Vector3(1, 1, 1));


//            add BulletComponent and link to the model comp xform
        ModelComponent mc = ee.getComponent(ModelComponent.class);
        Matrix4 transform = mc.modelInst.transform;
        transform.setTranslation(translation);
        BulletComponent
                bc = new BulletComponent(shape, transform, 1f); // how to set mass?
        ee.add(bc);

        /* add body to bullet world  default adds as 'OBJECT FLAG'*/
        BulletWorld.getInstance().addBodyWithCollisionNotif(
                ee // needs the Entity to add to the table BLAH
        );
    }
}
