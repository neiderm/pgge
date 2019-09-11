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
package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.sceneLoader.GameObject;
import com.mygdx.game.sceneLoader.InstanceData;
import com.mygdx.game.util.PrimitivesBuilder;

/*
 extends the sensor adaptor only for means of obtaining target
 */
public class DroppedThing extends SensorAdaptor {

    private int contactCount;
    private int contactBucket;

    private boolean exitXflag;

    /*
       Implmenetation notes WIP ...
        - allow jSON to define object that definately has no model instance . (ModelGroup:build() barfs .. )
     */
    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        debounceCollision(sensor);

        if (!exitXflag &&  // check flag to do this 1 time only!
                GameWorld.GAME_STATE_T.ROUND_COMPLETE_WAIT == GameWorld.getInstance().getRoundActiveState()) {
            exitXflag = true;

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

            // this is duplicated code ... refactor !!  (see GameObject)
            btCollisionObject body = bc.body;
            if (null != body) {
                // build a map associating these entities with an int index
                int next = BulletWorld.getInstance().userToEntityLUT.size;
///*
                body.setUserValue(next);
                body.setCollisionFlags(body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
                BulletWorld.getInstance().userToEntityLUT.add(sensor);
//*/
            }
        }
    }

    private void debounceCollision(Entity sensor) {

        if (contactCount > 0) {

            if (contactBucket > 0) { // each update, either empty 1 drop from the bucket and return, or if bucket empty then process it

                contactBucket -= 1;

            } else {

                if (0 == contactBucket) {

                    Gdx.app.log(" asdfdfd", "object is at rest ?? ");
                    contactCount = 0;

                    mkObject(sensor);   // some how make me an exit sensor


                    sensor.add(new StatusComponent(true)); // delete me!
                }
            }
        }
    }


    @Override
    public void onCollision(Entity myCollisionObject, int id) {

        Gdx.app.log("onCollision", "int = " + id);

        contactCount += 1; // always increment (zero'd out when bucket is emptied)

        // bucket fills faster to ensure that it takes time to empty the bucket once the contacts have rung out ....
        contactBucket = 60; // idfk ... allow at least 1 frames worth of updates to ensure object is at rest?
    }

    private static void mkObject(Entity target) {

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
}
