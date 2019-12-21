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
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;

/**
 * Created by neiderm on 7/5/2019.
 * <p>
 * This can be a "generic" handler for a sensor. assigned a single target Entity to be sensing for.
 */
public class KillSensor extends OmniSensor {

    int bucket; // debounce bucket

    @Override
    public void update(Entity sensor) {

        ModelComponent mc = sensor.getComponent(ModelComponent.class);

        // default bounding radius determined from mesh dimensions unless specified overridden in vS (tmp ... should be done in  base-class )
        if (mc.boundingRadius > 0 && vS.x == 0) {
            float adjRadius = mc.boundingRadius; // calc bound radius e.g. sphere will be larger than actual as it is based on dimensions of extents (box) so in many cases will look not close enuff ... but brute force collsision detect based on center-to-center dx of objects so that about as good as it gets (troubel detect collision w/  "longer" object e.g. the APC tank)
            this.omniRadius.set(adjRadius, adjRadius, adjRadius);
        }


        super.update(sensor);

        if (isTriggered) {

            if (bucket < 1) {

                StatusComponent sc = target.getComponent(StatusComponent.class);

                if (null == sc) {
                    sc = new StatusComponent(0);
                    target.add(sc); // default lifeclock should be 0
                }

                CompCommon.ImpactType impactType = CompCommon.ImpactType.DAMAGING;

                if (sc.lifeClock > 0) {
                    sc.lifeClock  -= 10;
                }
                if (sc.lifeClock <= 0) {
                    impactType = CompCommon.ImpactType.FATAL;
                }

                if (!sc.deleteMe) { // deleted entity may not have been removed from engine yet
                    CompCommon.makeBurnOut(
                            target.getComponent(ModelComponent.class).modelInst, impactType);
                }
                else
                    System.out.println("target entity already deleted");
            }

            bucket += 1;

        } else {
            bucket -= 2;
            if (bucket < 0) {
                bucket = 0;
            }
        }
    }
}
