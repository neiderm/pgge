/*
 * Copyright (c) 2021-2022 Glenn Neidermeier
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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.components.PhysicsComponent;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.util.ModelInstanceEx;

public class PhysProjectile extends FeatureAdaptor {

    private static final int IMPULSE_SCALE = 25;

    private final Vector3 vF = new Vector3();

    public PhysProjectile(Vector3 vFp) {
        this.vF.set(vFp);
    }

    private PhysicsComponent bc;
    private final Vector3 relPosition = new Vector3();
    private final Vector3 impulse = new Vector3();
    private final Vector3 tmpV = new Vector3();

    @Override
    public void update(Entity ee) {

        super.update(ee);

        // only do this once ... one time grab the entity bullet comp
        if (null == bc) {
            // set a collision processor
            if (null == collisionProcessor) {
                collisionProcessor = new DebouncedCollisionProc(0);
            }
            // impart a physics force
            bc = ee.getComponent(PhysicsComponent.class);

            if (null != bc && null != bc.body) {
                // one shot impulse
                bc.body.applyImpulse(impulse.set(vF).scl(IMPULSE_SCALE),
                        relPosition.set(0, 0, -1));
            }
        }
    }

    @Override
    public void onProcessedCollision(Entity ee) {

        CompCommon.physicsBodyMarkForRemoval(ee);

        ModelComponent mc = ee.getComponent(ModelComponent.class);
        ModelInstance mi = mc.modelInst;
        if (mi.materials.size > 0) {
            ModelInstanceEx.setColorAttribute(mi, new Color(Color.RED), 0.9f);
        }
        projectileUpdate(ee);
    }

    /*
     * copied from Projectile.update()
     */
    private void projectileUpdate(Entity projectile) {
        // projectile moves by one step vector if there is no collision imminent
        ModelComponent fmc = projectile.getComponent(ModelComponent.class);// could cache this model comp lookup?
// if (null != fmc)
        fmc.modelInst.transform.getTranslation(tmpV);
        btCollisionObject rayPickObject = BulletWorld.getInstance().rayTest(tmpV, vF, 1.0f);

        if (null != rayPickObject) {
            // stopped projectile
            StatusComponent sc = projectile.getComponent(StatusComponent.class);
            if (null != sc) {
                sc.lifeClock = 0; // kill off the projectile
            } else {
                projectile.add(new StatusComponent(0));
            }

            // projectile should make a ringy thing if hit wall or should shatter into triangles if impact on a shootable
            // unfortunately it seems walls are reported by the getCollisnEntity as valid target Entity   blah
            Entity target = BulletWorld.getInstance().getCollisionEntity(rayPickObject.getUserValue());

            if (null != target) {
                // spawn the sensor to handle the outcome
                CompCommon.spawnNewGameObject(
                        new Vector3(0.1f, 0.1f, 0.1f),
                        tmpV,   // mi.transform.getTranslation()
                        new KillSensor(target),
                        "capsule"); // the specified mesh-shape shown only momentarily ... could assign and use KIll Sensors' default shape-thingy here? for default Impact "STrike"
            } else {
                if (fmc.modelInst.materials.size > 0) {
                    ModelInstanceEx.setColorAttribute(fmc.modelInst, new Color(Color.PINK), 0.5f); //tmp?
                }
            }
        }
    }
}
