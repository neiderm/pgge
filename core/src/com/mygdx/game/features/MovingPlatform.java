/*
 * pgge
 *
 * Copyright (c) 2019 Glenn Neidermeier
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.ModelComponent;

public class MovingPlatform extends FeatureAdaptor {
    /*
     * simle platform moves from point a to point b.
     * non-simple platform would follow some kind of planned pattern
     */
    public MovingPlatform() { // mt
    }

    /* these shall be Vector3 so that the endpoints are simple A to B to A in 3D space and don't have to be on same plne */
//    private static final float Z_MIN = -5f;
//    private static final float Z_MAX = 15.0f;

    private static final float TRAVEL_STEP = 0.075f;

    private static final int STOP_TIME = 3 * 60; // FPS
    private int stopTimer = STOP_TIME;

    private static final float STEP_RAMP_INC = 0.005f;
    private float increment = STEP_RAMP_INC;


    private int travelDirection = 1;
    private Vector3 tmpV = new Vector3();

    // we travel point A to point B
    private Vector3 vA = new Vector3();
    private Vector3 vB = new Vector3();

    @Override
    public void update(Entity featureEnt) {
        //               super.update(featureIntrf);
        ModelInstance instance = featureEnt.getComponent(ModelComponent.class).modelInst;

        // sum of pointA and our offset vector which is by convention in vT
        vA.set(vT0);
        vB.set(vA);
        vB.add(vT);

        // see about dat
        float Z_MIN = vA.z;
        float Z_MAX = vB.z;

        if (stopTimer-- <= 0) {

            if (increment < TRAVEL_STEP) {
                increment += STEP_RAMP_INC;
            }

            float finalIncremnt = increment * travelDirection;

            instance.transform.trn(0, 0, finalIncremnt); // only moves visual model, not the body!

            tmpV = instance.transform.getTranslation(tmpV);
            float newZ = tmpV.z;

            if (newZ >= Z_MAX || newZ <= Z_MIN) {
                travelDirection *= -1;
                stopTimer = STOP_TIME;
                increment = STEP_RAMP_INC;

                // snap in the destination coordinate in case of overshooot
                if (newZ > Z_MAX)
                    tmpV.z = Z_MAX;

                if (newZ < Z_MIN)
                    tmpV.z = Z_MIN;

                instance.transform.setTranslation(tmpV);
            }

            BulletComponent bc = featureEnt.getComponent(BulletComponent.class);

            if (null != bc) {
                bc.body.setWorldTransform(instance.transform);
            }
        }
    }
}
