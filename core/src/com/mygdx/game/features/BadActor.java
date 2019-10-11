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
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.sceneLoader.GameFeature;

/*
 * here is a Bad Actor
 */
public class BadActor extends KillSensor {

    private Engine eeee = null;

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


        /*
         * migrate the following to Feature base class I guess ... basically then there is no need for this
         * class .... (pretty sure we will definately have to have a "Bad Actor" class in here somewhere!
         */



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

                // he's dead Jim
                addScore();

//                if (null != eeee)

                    CompCommon.makeBurnOut( badActor); //         gameObject.objectName = "sphereTex";
            }
        }
    }

    private void addScore(){

        GameFeature playerFeature = GameWorld.getInstance().getFeature("Player");

        if (null != playerFeature) {

            StatusComponent sc = playerFeature.getEntity().getComponent(StatusComponent.class);

            if (null != sc) {
                sc.UI.addScore(1500);
            }
        }
    }
}
