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

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.StatusComponent;

/*
 * here is a Bad Actor
 */
public class BadActor extends KillSensor {

    Engine eeee = null;

    @Override
    public void init(Object someObject) {

        if (null == target) {

            super.init(someObject);    // bidness as usual

        } else {
        /*
         target has already been set, use the "newly called" init() to set the 'Engine"
         what BS!
         somebody saying "I really hate java right now?!
        */
            eeee = (Engine) someObject;
        }
    }

    private StatusComponent sc = null;

    @Override
    public void update(Entity badActor) {

        super.update(badActor);

// is lame i guss for Comp lookup every update here, but presently we are not adding Status Comp to
// the entity , the SC is  being added dynamically for "explode()" feature ...
        // any problem building one into it ?... forgot exactly what triggers SC addition which inasmuch as i reclall is Player only

//        if (null == sc)
        {
            sc = badActor.getComponent(StatusComponent.class);
        }
//else
        if (null != sc)
        {
            if (sc.deleteMe) {

                ///// temp hhack to get the explode effect
//                if (null != eeee)
                {

//                    CompCommon.explode(eeee, badActor);

                    CompCommon.makeBurnOut( badActor);
                }
            }
        }
    }
}
