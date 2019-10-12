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
import com.mygdx.game.components.StatusComponent;

/*
 * here is a Bad Actor
 */
public class BadActor extends KillSensor {

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
//            eeee = (Engine) someObject;
        }
    }

    private StatusComponent sc = null;

    @Override
    public void update(Entity ee) {

        super.update(ee);

        /*
         * migrate the following to Feature base class I guess ... basically then there is no need for this
         * class .... (pretty sure we will definately have to have a "Bad Actor" class in here somewhere!
         */

// lame-o Comp lookup every update here, but presently we are not adding Status Comp to
// the entity , the SC is  being added dynamically for "explode()" feature ...
        /*
         * if I am pickable, then pick handler could have invoked this update() ... having added the
         * Status Comp + deleteMe ... why not let Status System handle it !!!!! e.g. "adapter.deactivate(Entity ee)"
         */

        StatusComponent sc = ee.getComponent(StatusComponent.class);

        // check this since the SC is actaully added dynamically (so no point to caching)
        if (null != sc) {

            if (sc.deleteMe) {

                // uses the Model Compont .transform translation so
                CompCommon.makeBurnOut(ee, 1500);
            }
        }
    }
}
