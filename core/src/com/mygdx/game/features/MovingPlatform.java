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
     * simle platform moves directly from point A to point B using simple proportional control
     * non-simple platform would follow some kind of planned pattern
     */
    public MovingPlatform() { // mt
    }

    private final float K_PROP = 0.04f; // 2b loaded from JSON

    private static final float RAMP_STEPS = 20; // whatever
    private static final int STOP_TIME = 3 * 60; // FPS

    private int stopTimer = STOP_TIME;
    private int nStepCnt;

    private Vector3 vA;// = new Vector3();
    private Vector3 vB;// = new Vector3();
    private Vector3 vStep = new Vector3();
    private Vector3 vError = new Vector3();
    private Vector3 tmpV = new Vector3();


    @Override
    public void update(Entity featureEnt) {
        //               super.update(featureIntrf);
        ModelInstance instance = featureEnt.getComponent(ModelComponent.class).modelInst;

        if (null == vA || null == vB){
            // bah grab the starting Origin (translation) of the entity from the instance data because we don't have proper init/constructions
            vA = new Vector3(vT0);
            vB = new Vector3(vT);
        }

        if (stopTimer-- <= 0) {

            instance.transform.getTranslation(tmpV);
            vError.set(vB).sub(tmpV);

            tmpV.set(Vector3.Zero);
            float dst2Error = tmpV.dst2(vError);

            // vA, vB fixed so this part would not change ... get the step vector (SAME DIRECTION as [ vB - vA ]
            // and scale by kProp
            vStep.set(vB);
            vStep.sub(vA);

            // take unit vector of (vB - vA)
            tmpV.set(Vector3.Zero);
            float dst2Step = tmpV.dst2(vStep);
            tmpV.set(vStep);
            tmpV.scl(1/(float)Math.sqrt(dst2Step));


            // apply kProp as gain on unit vector for constant speed (after the ramp) ... NOT on the
            // Error term ... so it's not really prop control!
            vStep.set(tmpV.scl(K_PROP));


            // scale vStep to ramping increment * ramp_count
            vStep.scl(1/RAMP_STEPS); // would be final/const .. using the dst2 value below

            // actual_step = ramp_inc_mag * number_of_steps_cnt
            if (nStepCnt < RAMP_STEPS) {
                nStepCnt += 1;
            }
            vStep.scl(nStepCnt);

            tmpV.set(Vector3.Zero);
            float dst2StepRampIncV = tmpV.dst2(vStep); // use this to test proximity to point B


            // use proportional output but capped at whatever the ramped value is
            tmpV.set(vError.scl(( K_PROP * 0.5f) ));
            if (tmpV.x <= vStep.x && tmpV.y <= vStep.y && tmpV.z <= vStep.z){
//                vStep.set(tmpV);
            }

            instance.transform.trn(vStep); // only moves visual model, not the body!

            if ( dst2Error < dst2StepRampIncV ) {

                stopTimer = STOP_TIME;
                nStepCnt = 0;

                //  we are there. swap end points
                tmpV.set(vA);
                vA.set(vB);
                vB.set(tmpV);
                instance.transform.setTranslation(vA); // snap-to
            }

            BulletComponent bc = featureEnt.getComponent(BulletComponent.class);

            if (null != bc) {
                bc.body.setWorldTransform(instance.transform);
            }
        }
    }
}
