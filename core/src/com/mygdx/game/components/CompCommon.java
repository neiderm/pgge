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

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.GameWorld;
import com.mygdx.game.features.ExitSensor;
import com.mygdx.game.sceneLoader.GameObject;
import com.mygdx.game.sceneLoader.InstanceData;
import com.mygdx.game.util.PrimitivesBuilder;

/*
 * the catch-all of the moment ...
 */
public class CompCommon {

    CompCommon(){ // mt
    }

    public static void explode(Engine engine, Entity picked /* , ModelComponent mc */){

        ModelComponent mc = picked.getComponent(ModelComponent.class);
        Vector3 translation = new Vector3();
        translation = mc.modelInst.transform.getTranslation(translation);
        translation.y += 0.5f; // offset Y so that node objects dont fall thru floor

        GameObject gameObject = new GameObject();
        gameObject.mass = 1f;
        gameObject.isShadowed = true;
        gameObject.scale = new Vector3(1, 1, 1);
        gameObject.objectName = "*";
        gameObject.meshShape = "convexHullShape";

        gameObject.buildNodes(engine, mc.model, translation, true);
        // remove intAttribute cullFace so both sides can show? Enable de-activation? Make the parts disappear?

        // mark dead entity for deletion
        picked.add(new StatusComponent(true));
    }

    public static void mkStaticFromDynamicEntity(Entity sss) {

        BulletComponent bc = sss.getComponent(BulletComponent.class);
        if (null == bc) {
            Gdx.app.log("collisionHdlr", "BulletComponent bc =  === NULLLL");
            return; // bah processing object that should already be "at rest" ???? .....................................................
        }

        bc.body.setCollisionFlags( bc.body.getCollisionFlags() & ~btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);

        // insert a newly created game OBject into the "spawning" model group
        GameObject gameObject = new GameObject();
        gameObject.isShadowed = true;

        Vector3 size = new Vector3(0.5f, 0.5f, 0.5f); /// size of the "box" in json .... irhnfi  bah
        gameObject.scale = new Vector3(size);

        gameObject.objectName = "box";

        Vector3 translation = new Vector3();
//                translation = bc.body.getWorldTransform().getTranslation(translation);

        ModelComponent mc = sss.getComponent(ModelComponent.class);
        Matrix4 tmpM4 = mc.modelInst.transform;
//            translation = tmpM4.getTranslation(translation);
        gameObject.getInstanceData().add(new InstanceData(tmpM4.getTranslation(translation)));

//                GameWorld.getInstance().addSpawner(gameObject);         ///    toooodllly dooodddd    object is added "kinematic" ???

        StatusComponent sc = new StatusComponent();
        sc.deleteFlag = 2;         // flag bullet Comp for deletion
        sss.add(sc);
    }

    public static void releasePayload(Entity target) {

        // insert a newly created game OBject into the "spawning" model group
        GameObject gameObject = new GameObject();
        gameObject.isShadowed = true;

        Vector3 size = new Vector3(0.5f, 0.5f, 0.5f); /// size of the "box" in json .... irhnfi  bah
        gameObject.scale = new Vector3(size);

//        gameObject.mass = 1; // let it be stationary

        gameObject.objectName = "sphere";

        Vector3 translation = new Vector3();
//                translation = bc.body.getWorldTransform().getTranslation(translation);

        ModelComponent mc = target.getComponent(ModelComponent.class);

        Matrix4 tmpM4 = mc.modelInst.transform;
        translation = tmpM4.getTranslation(translation);

        InstanceData id = new InstanceData(translation);
        ExitSensor es = new ExitSensor();
        es.init(target);
        es.vS.set(new Vector3(1.5f, 0, 0));
        id.adaptr = es;
        gameObject.getInstanceData().add(id);

        GameWorld.getInstance().addSpawner(gameObject); // toooodllly dooodddd    object is added "kinematic" ???
    }

    /* create a "bomb" ... creates a graphical mesh-shape and a matching
 physics body, adds it to the bullet world ... the drop is according to the height set above the target
 */
    public static void dropBomb(Entity sensor, Entity target){

        // set position above target, add bulletcomp to sensor
        Vector3 translation = new Vector3();
        BulletComponent bc = target.getComponent(BulletComponent.class);
        translation = bc.body.getWorldTransform().getTranslation(translation);

        ModelComponent mc = target.getComponent(ModelComponent.class);
        Matrix4 tmpM4 = mc.modelInst.transform;
        translation = tmpM4.getTranslation(translation);

        translation.y += 8; // idfkk ... make it fall from the sky!

        btCollisionShape shape = PrimitivesBuilder.getShape(
                "box", new Vector3(1, 1, 1));

//            add BulletComponent and link to the model comp xform
        mc = sensor.getComponent(ModelComponent.class);
        mc.modelInst.transform.setTranslation(translation);
        bc = new BulletComponent(shape, mc.modelInst.transform, 5.1f);
        bc.body.setWorldTransform(mc.modelInst.transform);
        sensor.add(bc);

        /* add body to bullet world (duplicated code ... refactor !!  (see GameObject)
         */
        btCollisionObject body = bc.body;
//        if (null != body)
        {
            // build a map associating these entities with an int index
            int next = BulletWorld.getInstance().userToEntityLUT.size;
            body.setUserValue(next);
            body.setCollisionFlags(body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
            BulletWorld.getInstance().userToEntityLUT.add(sensor);
        }
    }


}
