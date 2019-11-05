
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
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.util.ModelInstanceEx;

public class ShootamaThing extends VectorSensor {

    private Quaternion orientation = new Quaternion();
    private Vector3 down = new Vector3();
    private Vector3 xlation = new Vector3();
    private float originDegrees = -1;
    private final float ROTATION_STEP_INC = 0.25f;
    private float rotationStep = ROTATION_STEP_INC;

    private  float rotationMin = 0;
    private  float rotationMax = 0;
    private final int CAN_SHOOOT_INTERVAL = 64;
    private int canShootTimer = 0;

    float orientationAngle;

    int updown;



    @Override
    public void init(Object target) {

        super.init(target);
    }

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        if (originDegrees < 0){
            // love me some hacking .. one time init to get start rotation of thing
            ModelComponent mymc = sensor.getComponent(ModelComponent.class);
            Matrix4 myxfm = mymc.modelInst.transform;

           myxfm.getRotation(orientation);

            originDegrees = orientation.getAngle();
            rotationMin = originDegrees;
            rotationMax = originDegrees +       90 ;

            orientationAngle = originDegrees;

// bah
             if (orientation.y < 0){
                     updown = -1;}
                     else
                         updown = 1;


            System.out.println("shootamathing ... origin angle = " + originDegrees + " " + this.vT.x);
        }

        if (isActivated) {

            ModelComponent mc = sensor.getComponent(ModelComponent.class); // can this be cached?
// if (null != mc)
            if (mc.modelInst.materials.size > 0) {

                if (0 == canShootTimer) {

                    // well this is dumb its getting the insta.material anyway
                    ModelInstanceEx.setColorAttribute(mc.modelInst, new Color(Color.RED));

                } else {

                    // well this is dumb its getting the insta.material anyway
                    ModelInstanceEx.setColorAttribute(mc.modelInst, new Color(Color.ROYAL));
                }
            }
//else
//    Gdx.app.log("sdf", "sdf"); //  doesn't necessarily have a material

            if (isTriggered ||
                    canShootTimer > 0) {

                if (canShootTimer > 0)
                {
                    canShootTimer -= 1;

                } else {

                    canShootTimer = CAN_SHOOOT_INTERVAL;

                    gunSight(sensor, target);
                }

                if (mc.modelInst.materials.size > 0) {
// well this is dumb its getting the insta.material anyway
                    ModelInstanceEx.setColorAttribute(mc.modelInst, new Color(Color.GOLD)); // tmp test code
                }

                isTriggered = false; // need to unlatch it !
            }

            /*
             * if I am pickable, then pick handler could have invoked this update() ... having added the
             * Status Comp + deleteMe ... why not let Status System handle it !!!!!
             */
            StatusComponent sc = sensor.getComponent(StatusComponent.class);

            // check this since the SC is actaully added dynamically (so no point to caching)
            if (null != sc && sc.deleteMe) {

                // uses the Model Compont .transform translation so
                CompCommon.makeBurnOut(sensor, 1000);
            }
            // else System.out.println();
        }

        ModelComponent mymc = sensor.getComponent(ModelComponent.class);

        updatePlatformRotation(mymc.modelInst.transform);
    }

        /*
        toodoo use raycast to determine dx to stop of cast and if target comes within x degrees of
        whatever direction it happens to be pointing, then it begins rotating to track target

        turn toward the target
         */
    private void updatePlatformRotation(Matrix4 myxfm){

 //       float orientationAngle = orientation.getAngle();
        orientationAngle += rotationStep;

        if (orientationAngle > rotationMax) {
//            System.out.println("shootamathing ...  angle > rotationMax " + orientationAngle + " " + this.vT.x);

            orientationAngle = rotationMax;
            rotationStep = -ROTATION_STEP_INC;

            canShootTimer = 0;

        } else if (orientationAngle < rotationMin) {
//            System.out.println("shootamathing ... angle < rotationMIN " + orientationAngle + " " + this.vT.x);

            orientationAngle = rotationMin;
            rotationStep = ROTATION_STEP_INC;

            canShootTimer = 0;
        }


        myxfm.getRotation(orientation);
        orientation.getAxisAngle(tmpV);

        xlation = myxfm.getTranslation(new Vector3(xlation));

        if (updown < 0) {
//            myxfm.setToRotation(tmpV, orientationAngle );
//            myxfm.setToRotation(down.set(0, -1, 0), orientationAngle);
            myxfm.setFromEulerAngles(orientationAngle + 90, 0, 0);
         }
        else {
//            myxfm.setToRotation(tmpV, orientationAngle);
//            myxfm.setToRotation(down.set(0, 1, 0), orientationAngle) ;
            myxfm.setFromEulerAngles(orientationAngle + 0, 0, 0);
        }

        myxfm.setTranslation(new Vector3(xlation));

        myxfm.getRotation(orientation);

//        myxfm.rotate(down.set(0, 1, 0), rotationStep);
    }


    private void __updatePlatformRotation(Matrix4 myxfm){

        myxfm.getRotation(orientation);

        float rad = orientation.getAngleRad();
        float orientationAngle = orientation.getAngle();


        myxfm.rotate(down.set(0, 1, 0), ROTATION_STEP_INC);
    }


    private Matrix4 tmpM = new Matrix4();
    private Vector3 trans = new Vector3();
    private Vector3 tmpV = new Vector3();

    // allowing this to be here so it can be basis of setting forwared vector for projectile/weaopon
    private void gunSight(Entity source, Entity target) {

        ModelComponent mc = source.getComponent(ModelComponent.class);

        if (null != mc && null != mc.modelInst) {
            tmpM = mc.modelInst.transform;
        }

        tmpM.getRotation(orientation);

        // offset the trans  because the model origin is free to be adjusted in Blender e.g. at "surface level"
        // depending where on the model origin is set (done intentionally for adjustmestment of decent steering/handling physics)
        tmpV.set(0, +0.001f, 0); // using +y for up vector ...

        ModelInstanceEx.rotateRad(tmpV, orientation); // ... and rotsting the vector to orientation of transform matrix
        tmpM.getTranslation(trans).add(tmpV); // start coord of projectile now offset "higher" wrt to vehicle body

        // set unit vector for direction of travel for theoretical projectile fired perfectly in forwared direction
        //              float mag = -0.1f; // scale the '-1' accordingly for magnitifdue of forward "velocity"
//                Vector3 vvv = ModelInstanceEx.rotateRad(tmpV.set(0, 0, mag), orientation); // don't need to get Rotaion again ;)
        /*
         * pass "picked" thing to projectile to use as sensor target (so it's actually only sensing for the one target!
         */
        CompCommon.spawnNewGameObject(new Vector3(0.2f, 0.2f, 0.2f),
                trans,
                new Projectile(target, tmpM),
                "sphere");
    }
}
