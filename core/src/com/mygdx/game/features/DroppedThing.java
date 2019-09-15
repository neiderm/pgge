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
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.StatusComponent;

/*
 extends the sensor adaptor only for means of obtaining target
 */
/* Bomb ... bomb w/ payloaad (payloadcan be exit sensor ;) */
public class DroppedThing extends SensorAdaptor {

    private int contactCount;
    private int contactBucket;


    /*
       Implmenetation notes WIP ...
        - allow jSON to define object that definately has no model instance . (ModelGroup:build() barfs .. )
     */
    @Override
    public void update(Entity sensor /* bombEntity ;)  */) {

        super.update(sensor);

        debounceCollision(sensor);
        /*
        if (null != collisionProcessor){
            collisionProcessor.debounceCollision(ee);
        }
         */
    }

    @Override
    public void onActivate(Entity ee) {

        FeatureAdaptor newFa = getFeatureAdapter(this);

        CompCommon.dropBomb(ee, target); // entityAddRigidBody
    }

    private void debounceCollision(Entity sensor) {

        if (contactCount > 0) {

            if (contactBucket > 0) { // each update, either empty 1 drop from the bucket and return, or if bucket empty then process it

                contactBucket -= 1;

            } else
                if (0 == contactBucket) {

                    Gdx.app.log(" asdfdfd", "object is at rest ?? ");
                    contactCount = 0;

                    CompCommon.releasePayload(sensor);   // spawnNewGameObject

                    sensor.add(new StatusComponent(true)); // delete me!
                }
        }
    }

    /*
        ???????
                                if (null != fa) {
                                    fa.collisionProcessor.onCollision(ee, 1); //
                                }
     */
    @Override
    public void onCollision(Entity myCollisionObject, int id) {

        Gdx.app.log("onCollision", "int = " + id);

        contactCount += 1; // always increment (zero'd out when bucket is emptied)

        // bucket fills faster to ensure that it takes time to empty the bucket once the contacts have rung out ....
        contactBucket = 60; // idfk ... allow at least 1 frames worth of updates to ensure object is at rest?
    }
}
