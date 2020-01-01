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
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.util.ModelInstanceEx;

public class ShootamaThing extends VectorSensor {

    private Quaternion orientation = new Quaternion();
    private float orientationAngle = -1;  // dumb ;)
    private final float ROTATION_STEP_DEGREES = 0.25f;
    private float rotationStep = ROTATION_STEP_DEGREES;
    private final float ROTATION_RANGE_DEGREES = 90.0f;
    private float rotationMin = 0;
    private float rotationMax = 0;
    private final int SHOT_RECYCLE_TIME = 64;
    private int shotRecycleTimer = 0;


    @Override
    public void init(Object target) {

        super.init(target);
    }

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        ModelComponent mc = sensor.getComponent(ModelComponent.class);

        if (orientationAngle < 0) { // so dumb
            // one time init to rotation params
            mc.modelInst.transform.getRotation(orientation);

            tmpV.set(0, -1, 0); // todo: get the actual "down" vector e.g. in case on inclined sfc.
            orientationAngle = orientation.getAngleAround(tmpV);

            rotationMin = orientationAngle;
            rotationMax = orientationAngle + ROTATION_RANGE_DEGREES;

            System.out.println("shootamathing ... origin angle = " + orientationAngle + " " + this.vT.x);
        }


// if (null != mc) {
        if (mc.modelInst.materials.size > 0) {

            if (0 == shotRecycleTimer) {

                ModelInstanceEx.setColorAttribute(mc.modelInst, new Color(Color.RED));
            } else {
                ModelInstanceEx.setColorAttribute(mc.modelInst, new Color(Color.ROYAL));
            }
        }
// }
//else //    Gdx.app.log("sdf", "sdf"); //  doesn't necessarily have a material

        if (isTriggered ||
                shotRecycleTimer > 0) {

            if (shotRecycleTimer > 0) {
                shotRecycleTimer -= 1;

            } else {
                shotRecycleTimer = SHOT_RECYCLE_TIME;

                gunSight(mc.modelInst.transform, target);
            }

            if (mc.modelInst.materials.size > 0) {
// well this is dumb its getting the insta.material anyway
                ModelInstanceEx.setColorAttribute(mc.modelInst, new Color(Color.GOLD)); // tmp test code
            }

            isTriggered = false; // need to unlatch it !
        }

        updatePlatformRotation(mc.modelInst.transform);

        BulletComponent bc = sensor.getComponent(BulletComponent.class);

        if (null != bc) {
            bc.body.setWorldTransform(mc.modelInst.transform);
        }

    }

    /*
    toodoo use raycast to determine dx to stop of cast and if target comes within x degrees of
    whatever direction it happens to be pointing, then it begins rotating to track target
     */
    private void updatePlatformRotation(Matrix4 myxfm) {

        myxfm.getRotation(orientation);
        trans = myxfm.getTranslation(trans); // note: sets identity so trans & scale lost!

        tmpV.set(0, -1, 0); // todo: get the actual "down" vector e.g. in case on inclined sfc.

        orientationAngle += rotationStep;

        if (orientationAngle > rotationMax) {
            rotationStep = -ROTATION_STEP_DEGREES;
            shotRecycleTimer = 0;
// snap it to its endpoint
            orientationAngle = rotationMax;

            myxfm.setToRotation(tmpV, orientationAngle); // note: sets identity so trans & scale lost!
            myxfm.setTranslation(trans);

        } else if (orientationAngle < rotationMin) {
            rotationStep = ROTATION_STEP_DEGREES;
            shotRecycleTimer = 0;
// snap it to its endpoint
            orientationAngle = rotationMin;

            myxfm.setToRotation(tmpV, orientationAngle); // note: sets identity so trans & scale lost!
            myxfm.setTranslation(trans);
        }

        myxfm.rotate(tmpV, rotationStep);
    }


    private Vector3 trans = new Vector3();
    private Vector3 tmpV = new Vector3();

    // allowing this to be here so it can be basis of setting forwared vector for projectile/weaopon
    private void gunSight(Matrix4 sourceM, Entity target) {

            sourceM.getRotation(orientation);

            // offset the trans  because the model origin is free to be adjusted in Blender e.g. at "surface level"
            // depending where on the model origin is set (done intentionally for adjustmestment of decent steering/handling physics)
            tmpV.set(0, +0.001f, 0); // using +y for up vector ...

            ModelInstanceEx.rotateRad(tmpV, orientation); // ... and rotsting the vector to orientation of transform matrix
            sourceM.getTranslation(trans).add(tmpV); // start coord of projectile now offset "higher" wrt to vehicle body

            // set unit vector for direction of travel for theoretical projectile fired perfectly in forwared direction
            //              float mag = -0.1f; // scale the '-1' accordingly for magnitifdue of forward "velocity"
//                Vector3 vvv = ModelInstanceEx.rotateRad(tmpV.set(0, 0, mag), orientation); // don't need to get Rotaion again ;)
            /*
             * pass "picked" thing to projectile to use as sensor target (so it's actually only sensing for the one target!
             */
            CompCommon.spawnNewGameObject(
                    new Vector3(0.2f, 0.2f, 0.2f),
                    trans,
                    new Projectile(target, sourceM),
                    "sphere");
    }
}
