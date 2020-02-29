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
    private final float ROTATION_STEP_DEGREES = 0.25f;
    private float rotationStep = ROTATION_STEP_DEGREES;
    private final float ROTATION_RANGE_DEGREES = 90.0f;
    private float rotationMin = 0;
    private float rotationMax = 0;
    private final int SHOT_RECYCLE_TIME = 64;
    private int shotRecycleTimer = 0;
    private ModelComponent mc;
    private Color gcolor = new Color();


//    @Override
//    public void init(Object target) {
//
//        super.init(target);
//    }

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        if (null == mc) {
            // one time inits
            mc = sensor.getComponent(ModelComponent.class);
            mc.modelInst.transform.getRotation(orientation);

            //  for simplicity, min is always set to starting angle, i.e. max equal (min + Range)
            float orientationAngle = orientation.getAngleAround(tmpV.set(0, -1, 0)); // todo: get the actual "down" vector e.g. in case on inclined sfc.
            rotationMin = orientationAngle;
            rotationMax = orientationAngle + ROTATION_RANGE_DEGREES;

            if (rotationStep < 0) {
                // if initial value of RotationStep is negative, "pre-rotate" to i.e. Max and commence rotation from there?
            } else if (rotationStep > 0) {
                //
            }

        }
//        if (null != mc)
        {
            // tmp test code
            if (/*null != mc &&*/ mc.modelInst.materials.size > 0) {
                gcolor.set(Color.RED);
                if (0 == shotRecycleTimer) {
                    gcolor.set(Color.GOLD);
                }
                ModelInstanceEx.setColorAttribute(mc.modelInst, gcolor); // tmp test code
            }

            if (isTriggered ||
                    shotRecycleTimer > 0) {

                if (shotRecycleTimer > 0) {
                    shotRecycleTimer -= 1;
                } else {
                    shotRecycleTimer = SHOT_RECYCLE_TIME;

                    gunSight(mc.modelInst.transform, target);
                }

                isTriggered = false; // need to unlatch it !
            }


            // update the rotating platform and re-sync bullet shape with model instance that we've been rotating/moving
            rotationStep =
                    updatePlatformRotation(rotationStep, rotationMin, rotationMax, mc.modelInst.transform);


            mc.modelInst.calculateTransforms(); // definately need this !

            BulletComponent bc = sensor.getComponent(BulletComponent.class);

            if (null != bc) {


                bc.body.setWorldTransform(mc.modelInst.transform);
            }
        }
    }

    /*
    toodoo use raycast to determine dx to stop of cast and if target comes within x degrees of
    whatever direction it happens to be pointing, then it begins rotating to track target
     */
    private float updatePlatformRotation(float rStep, float rMin, float rMax, Matrix4 myxfm) {

        Vector3 down = tmpV.set(0, -1, 0); // todo: get the actual "down" vector e.g. in case on inclined sfc.
        trans = myxfm.getTranslation(trans); // note: sets identity so trans & scale lost!
        myxfm.rotate(down, rStep); // take the step (up here, down there, shouldn't make any diff)

        /*
         * Min  and Max are setup such that ...
         *   0 < rotMin < rotMax
         * ... and  rotMax may be > 360
         */
        myxfm.getRotation(orientation);
        float fAngle = orientation.getAngleAround(down);
        if (rStep > 0 && (fAngle < rMin)) {
            fAngle += 360;
        }

        if (fAngle > rMax) {
            rStep = (-1) * Math.abs(rStep); // probably should just do a straight sign flip
//            shotRecycleTimer = 0;                  // how to signify to caller that endpoint has been hit?
// snap it to its endpoint
            myxfm.setToRotation(down, rMax); // note: sets identity so trans & scale lost!
            myxfm.setTranslation(trans);

        } else if (fAngle < rMin) {
            rStep = (+1) * Math.abs(rStep);// // probably should just do a straight sign flip
//            shotRecycleTimer = 0;                  // how to signify to caller that endpoint has been hit?
// snap it to its endpoint
            myxfm.setToRotation(down, rMin); // note: sets identity so trans & scale lost!
            myxfm.setTranslation(trans);
        }

//        myxfm.rotate(down, rStep); // take the step (up there, down here, shouldn't make any diff)

        return rStep;
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
                new KillSensor(target, sourceM),
                "sphere");
    }
}
