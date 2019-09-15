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
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.sceneLoader.GameObject;
import com.mygdx.game.sceneLoader.InstanceData;

public class DebouncedCollisionProc implements CollisionProcessorIntrf {

    private int contactCount;
    private int contactBucket;

    @Override
    public void onCollision(Entity myCollisionObject) {

        contactCount += 1; // always increment (zero'd out when bucket is emptied)

        // bucket fills faster to ensure that it takes time to empty the bucket once the contacts have rung out ....
        contactBucket = 60; // idfk ... allow at least 1 frames worth of updates to ensure object is at rest?
    }

    @Override
    public void processCollision(Entity ee) {

        if (contactCount > 0) {

            if (contactBucket > 0) { // each update, either empty 1 drop from the bucket and return, or if bucket empty then process it

                contactBucket -= 1;

            } else
            if (0 == contactBucket) {

                Gdx.app.log(" asdfdfd", "object is at rest ?? ");
                contactCount = 0;

                spawnNewGameObject(ee); // spawnNewGameObject

                ee.add(new StatusComponent(true)); // delete me!
            }
        }
    }


    public static void spawnNewGameObject(Entity ee) {

        // insert a newly created game OBject into the "spawning" model group
        GameObject gameObject = new GameObject();
        gameObject.isShadowed = true;

        Vector3 size = new Vector3(0.5f, 0.5f, 0.5f); /// size of the "box" in json .... irhnfi  bah
        gameObject.scale = new Vector3(size);

//        gameObject.mass = 1; // let it be stationary

        gameObject.objectName = "sphere";

        Vector3 translation = new Vector3();
//                translation = bc.body.getWorldTransform().getTranslation(translation);

        ModelComponent mc = ee.getComponent(ModelComponent.class);

        Matrix4 tmpM4 = mc.modelInst.transform;
        translation = tmpM4.getTranslation(translation);

        InstanceData id = new InstanceData(translation);

        ExitSensor es = new ExitSensor();

        es.init(ee);
        es.vS.set(new Vector3(1.5f, 0, 0));
        id.adaptr = es;
        gameObject.getInstanceData().add(id);

        GameWorld.getInstance().addSpawner(gameObject); // toooodllly dooodddd    object is added "kinematic" ???
    }

}
