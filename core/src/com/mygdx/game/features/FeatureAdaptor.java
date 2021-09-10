/*
 * Copyright (c) 2021 Glenn Neidermeier
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

    public enum F_SUB_TYPE_T {
        FT_NONE,
        FT_RESERVED,
        FT_EXIT,
        FT_SLIDEY_BLK,
        FT_PLAYER,
        FT_ACTOR,
        FT_WEAAPON_0,
        FT_WEAAPON_1
    }

    public F_SUB_TYPE_T fSubType;

    public CollisionProcessorIntrf collisionProcessor;
    public int bounty;

    // generic integer attributes  ? e.g min/max etc. idfk ...  non-POJO types must be new'd if instantiate by JSON
    final Vector3 vR = new Vector3(); // x = offset of omnisensor sense zone from body origin
    final Vector3 vS = new Vector3();// x = radius of omnisensor

    // origins or other gameObject.instance specific data - position, scale etc.
//    public Vector3 vR0 = new Vector3();
//    public Vector3 vS0 = new Vector3();
    final Vector3 vT0 = new Vector3(); // starting Origin (translation) of the entity from the instance data

    GameWorld.GAME_STATE_T activateOnState;
    boolean isActivated;
    Vector3 vT = new Vector3(); // sensor location thing, or projectile movement step

    @Override
    public void init(Object asdf) { // mt
    }

    @Override
    public void update(Entity ee) { // mt

        // allow not defined in json to be implicitly ignoired,
        if (!isActivated &&
                (activateOnState == GameWorld.getInstance().getRoundActiveState() ||
                        activateOnState == GameWorld.GAME_STATE_T.ROUND_ACTIVATE_ON_ALL)) {
            isActivated = true;
            onActivate(ee);
        }

        if (null != collisionProcessor && collisionProcessor.processCollision(ee)) {
            onProcessedCollision(ee);
        }
    }

    public void onDestroyed(Entity e) { // mt
    }

    public void onProcessedCollision(Entity ee) { // mt
    }

    @Override
    public void onActivate(Entity ee) { // mt
    }

    public FeatureAdaptor makeFeatureAdapter(Vector3 position) {

        if (null == activateOnState) {
            isActivated = true; // default to "activated" if no activation trigger is specified
        }

        // grab the starting Origin (translation) of the entity from the instance data
        vT0.set(position);
        init(userData);

        return this;
    }
}
