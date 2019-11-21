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
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.util.ModelInstanceEx;

/*
 * original prototype for killin things
 */
public class ElectricEye extends  VectorSensor {

    int prev; // hack crap
    int bucket;

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        if (isActivated) {

            ModelComponent mc = sensor.getComponent(ModelComponent.class); // can this be cached?
// if (null != mc)
            if (mc.modelInst.materials.size > 0) {
// well this is dumb its getting the insta.material anyway
                ModelInstanceEx.setColorAttribute(mc.modelInst, new Color(Color.ROYAL)); // tmp test code
            }
//else
//    Gdx.app.log("sdf", "sdf"); //  doesn't necessarily have a material

            if (isTriggered) {

                if (mc.modelInst.materials.size > 0) {
// well this is dumb its getting the insta.material anyway
                    ModelInstanceEx.setColorAttribute(mc.modelInst, new Color(Color.GOLD)); // tmp test code
                }

                isTriggered = false; // VectorSensor not unlatching itself!

                if (bucket < 1) {
                    // clock target probly for player, other wise probly no status comp
                    StatusComponent sc = target.getComponent(StatusComponent.class);

                    if (null != sc) {
                        int lc = target.getComponent(StatusComponent.class).lifeClock;
                        lc -= 10;
                        if (lc > 0){ // don't Burn Out on final hit (explodacopia)
                            // use the target model instance texture etc.
                            CompCommon.makeBurnOut(
                                    target.getComponent(ModelComponent.class).modelInst, CompCommon.ImpactType.DAMAGING);
                        }else{
                            lc = 0;
                        }
                        target.getComponent(StatusComponent.class).lifeClock = lc;
                    }
                }
                bucket += 1;

            } else {
                bucket -= 2;
                if (bucket < 0) {
                    bucket = 0;
                }
            }

            /*
             * kill me
             */
            StatusComponent sc = sensor.getComponent(StatusComponent.class);

            // check this since the SC is actaully added dynamically (so no point to caching)
            if (null != sc) {

                if (0 == sc.lifeClock) {

                    if (0 == prev++) {// bah crap

                        sc.bounty = 1000;
                        // uses the Model Compont .transform translation so
                        CompCommon.makeBurnOut(
                                sensor.getComponent(ModelComponent.class).modelInst, CompCommon.ImpactType.FATAL);
                    }
                }
            }
            // else System.out.println();
        }
    }
}
