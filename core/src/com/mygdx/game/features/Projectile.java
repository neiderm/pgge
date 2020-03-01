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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.util.ModelInstanceEx;

public class Projectile extends OmniSensor {

    // working variables
    private final Vector3 tmpV = new Vector3();
    private final Quaternion orientation = new Quaternion();
    private final Vector3 vF = new Vector3();


    public Projectile(Vector3 vFp) {

        this.vF.set(vFp);
    }

    /*
     * doesn't do much but get a vector for the shooters forwared-orientation and scale to projectile movement delta vector
     */
    // probably get rid of this one
    private Vector3 getDirectionVector(Matrix4 shootersTransform) {

        Vector3 vvv = new Vector3();

        shootersTransform.getRotation(orientation);

        // set unit vector for direction of travel for theoretical projectile fired perfectly in forwared direction
        float mag = -0.15f; // scale the '-1' accordingly for magnitifdue of forward "velocity"

        vvv.set(ModelInstanceEx.rotateRad(tmpV.set(0, 0, mag), orientation));

        return vvv;
    }


    @Override
    public void update(Entity projectile) {

        // projectile moves by one step vector if there is no collision imminent
        ModelComponent fmc = projectile.getComponent(ModelComponent.class);// could cache this model comp lookup?
// if (null != fmc)
        fmc.modelInst.transform.getTranslation(tmpV);
        btCollisionObject rayPickObject = BulletWorld.getInstance().rayTest(tmpV, vF, 1.0f);

        if (null == rayPickObject) {
            // no collision imminient so keep it moving along
            fmc.modelInst.transform.trn(vF);
        } else {
            // stopped projectile
            if (fmc.modelInst.materials.size > 0) {
                ModelInstanceEx.setColorAttribute(fmc.modelInst, new Color(Color.PINK), 0.5f); //tmp?
            }

            StatusComponent sc = projectile.getComponent(StatusComponent.class);
            if (null != sc) {
                sc.lifeClock = 0;    // kill off the projectile
            } else {
                projectile.add(new StatusComponent(0));
            }

// projectile should make a ringy thing if hit wall or should shatter into triangles if impact on a shootable

            Entity target = BulletWorld.getInstance().getCollisionEntity(rayPickObject.getUserValue());
            if (null != target) {
                // spawn the sensor to handle the outcome
                CompCommon.spawnNewGameObject(
                        new Vector3(0.1f, 0.1f, 0.1f),
                        tmpV,   //                                mi.transform.getTranslation(trans),
                        new KillSensor(target), // this projectile doesn't move ;)
                        "capsule");
            }
        }
    }
}
