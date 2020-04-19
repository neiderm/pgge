/*
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
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.GameWorld;

public class FeatureAdaptor implements FeatureIntrf {

    private Object userData; // lefttover  hackage
    public int bounty;

    GameWorld.GAME_STATE_T activateOnState;
    boolean isActivated;

    public CollisionProcessorIntrf collisionProcessor;

    // generic integer attributes  ? e.g min/max etc. idfk ...  non-POJO types must be new'd if instantiate by JSON
    Vector3 vR = new Vector3(); // x = offset of omnisensor sense zone from body origin
    Vector3 vS = new Vector3();// x = radius of omnisensor
    Vector3 vT = new Vector3(); // sensor location thing, or projectile movement step

    // origins or other gameObject.instance specific data - position, scale etc.
//    public Vector3 vR0 = new Vector3();
//    public Vector3 vS0 = new Vector3();
    Vector3 vT0 = new Vector3();     // starting Origin (translation) of the entity from the instance data


    public enum F_SUB_TYPE_T {
        FT_NONE,
        FT_RESERVED,
        FT_PLAYER,
        FT_ACTOR,
        FT_WEAAPON_0,
        FT_WEAAPON_1;
    };


    public F_SUB_TYPE_T fSubType;


    @Override
    public void init(Object asdf) { // mt
    }


    @Override
    public void update(Entity ee) { // mt

        // allow not defined in json to be implicitly ignoired,
        if (!isActivated &&
                (activateOnState == GameWorld.getInstance().getRoundActiveState() ||
                        activateOnState == GameWorld.GAME_STATE_T.ROUND_ACTIVATE_ON_ALL)
        ) {

            isActivated = true;
            onActivate(ee);
        }

        if (null != collisionProcessor) {

            if (collisionProcessor.processCollision(ee)) {

                onProcessedCollision(ee);
            }
        }
    }


    public void onDestroyed(Entity e) { // mt
    }

    public void onProcessedCollision(Entity ee) { // mt
    }


    @Override
    public void onActivate(Entity ee) { // mt
    }


    /*
     * leftover from hackage
     */
    public FeatureAdaptor makeFeatureAdapter(Vector3 position) {

        if (null == activateOnState) {
            isActivated = true; // default to "activated" if no activation trigger is specified
        }

// hope init() won't clobber  vt0
        // grab the starting Origin (translation) of the entity from the instance data
        vT0.set(position);

// big hack ... idfk... need some kind of generic means to let the Feature Adapter sub-class take care of its derived implementation
        init(userData);

        return this;
    }
}
