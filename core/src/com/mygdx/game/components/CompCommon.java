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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.GameWorld;
import com.mygdx.game.features.BurnOut;
import com.mygdx.game.features.FeatureAdaptor;
import com.mygdx.game.sceneLoader.GameFeature;
import com.mygdx.game.sceneLoader.GameObject;
import com.mygdx.game.sceneLoader.InstanceData;
import com.mygdx.game.sceneLoader.ModelInfo;
import com.mygdx.game.sceneLoader.SceneData;
import com.mygdx.game.util.PrimitivesBuilder;

/*
 * the catch-all of the moment ...
 */
public class CompCommon {

    CompCommon() { // mt
    }

    public static void exploducopia(ModelInstance modelInst, int modelInfoIndx) {

        // has local translation but also need to set in instance w/ the "parent" instance translation
        // tmp test ... can retrieve the model name info whatever somehow embed into the new object?
        int countIndex = 0;
        int targetModelGroupIndex = modelInfoIndx;
        SceneData sd = GameWorld.getInstance().getSceneData();
        String targetMdlInfoKey = null;

        for (String key : sd.modelInfo.keySet()) {
            if (targetModelGroupIndex == countIndex) {
                ModelInfo mi = sd.modelInfo.get(key);
                targetMdlInfoKey = key;
                break;
            }
            countIndex += 1;
        }

/*
 this is still wonky and need generalized more
 */
        if (null != targetMdlInfoKey) {

            Vector3 translation = new Vector3();
            modelInst.transform.getTranslation(translation);
            translation.y += 0.5f; // offset Y so that node objects dont fall thru floor

            spawnNewGameObject(
                    null, translation, "*", targetMdlInfoKey, true);
        } else {
            System.out.println("no targetMdlInfoKey found");
            makeBurnOut(modelInst, 100);
        }

        // remove intAttribute cullFace so both sides can show? Enable de-activation? Make the parts disappear?
        // mark dead entity for deletion ... do it here?
//        picked.add(new StatusComponent(true));       ........ nope ... caller decides if/how to dispose of its target
    }

    /*
    killWithPoints ... gaBoom !
    The thing that is going 'gaBoom' should be able to specify Material texture,  Color Attr. only)
    (or else if no Texture Attrib. then we assign a default (fire-y!!) one! ?
    */
    public static void makeBurnOut(Entity ee, int points) {

        makeBurnOut(ee.getComponent(ModelComponent.class).modelInst, points);
    }

    public static void makeBurnOut(ModelInstance mi, int points) {

        if (0 == points){
            mi.userData = new Color(Color.YELLOW); // hacky hackhackster
        }

        String tmpObjectName = "sphere";

        spawnNewGameObject(

                mi, // just for the translation

                // must be a non-anonynoyus class to work thru gameobject.build. For this default use, it is
                // ok to have a constructor for a specific type of argument ...
                new BurnOut(mi),  // ... this one happens to do special sauce w/ the so-called 'userData' hackamathing (to get mesh/texture info to render the efferct!)

                tmpObjectName);

        GameFeature playerFeature = GameWorld.getInstance().getFeature("Player"); // LOCAL_PLAYER_FNAME

        if (null != playerFeature) {

            StatusComponent psc = playerFeature.getEntity().getComponent(StatusComponent.class);

            if (null != psc) {
                if (null == psc.UI)
                    System.out.println(); // i think race condition, confirm assert ... somehow player kills BA, but BA is kills player, so SC->UI going invalid
                else {
                    psc.UI.addScore(points);
                }
            }
        }
    }

    /*
     * Object creator for dynamic spawning
     * Caller can specify the Feature Adapter, mesh shape and material to use, (or let defaults) thus
     * allowing the  type of explosion or whatever effect to be per-caller.
     * Here an Game Object is made but not the engtity yet, as delayed-queued for spawnning.
     */
    public static void spawnNewGameObject(ModelInstance mi, FeatureAdaptor fa, String objectName) {

        Vector3 translation = new Vector3(); // tmp for new vector instance .. only need to feed the GC relavitvely few of thsesei guess

        spawnNewGameObject(
                new Vector3(1, 1, 1),
                mi.transform.getTranslation(translation),
                fa,  // pass-thru
                objectName);
    }

    private static void spawnNewGameObject(
            Vector3 scale, Vector3 translation, String objectName, String modelInfoKey, boolean useLOcalTranslation) {

        // insert a newly created game OBject into the "spawning" model group
        GameObject gameObject = new GameObject(/* objectName */);
        gameObject.useLocalTranslation = useLOcalTranslation;
        gameObject.objectName = objectName;

// fixed for now ;
        gameObject.mass = 1f;
        gameObject.meshShape = "convexHullShape";

        if (null != scale) {
            gameObject.scale = new Vector3(scale);
        } else {
            gameObject.scale = new Vector3(1, 1, 1);
        }

        //        gameObject.mass = 1; // let it be stationary
        gameObject.getInstanceData().add(new InstanceData(translation));

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
        GameObject gameObject = new GameObject(/* objectName */);
        gameObject.objectName = objectName;
        gameObject.scale = scale;

        //        gameObject.mass = 1; // let it be stationary

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
