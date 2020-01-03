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
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;

public class FeatureAdaptor implements FeatureIntrf {

    Object userData; // lefttover  hackage
    public int bounty;

    private GameWorld.GAME_STATE_T activateOnState;
    protected KillSensor.ImpactType impactType;
    boolean isActivated;

    public CollisionProcessorIntrf collisionProcessor;

    // generic integer attributes  ? e.g min/max etc. idfk ...  non-POJO types must be new'd if instantiate by JSON
    public Vector3 vR = new Vector3(); // x = offset of omnisensor sense zone from body origin 
    public Vector3 vS = new Vector3();// x = radius of omnisensor
    public Vector3 vT = new Vector3(); // sensor location thing, or projectile movement step  

    // origins or other gameObject.instance specific data - position, scale etc.
//    public Vector3 vR0 = new Vector3();
//    public Vector3 vS0 = new Vector3();
    public Vector3 vT0 = new Vector3();     // starting Origin (translation) of the entity from the instance data


    @Override
    public void init(Object asdf) { // mt
    }


    @Override
    public void update(Entity ee) { // mt

        // allow not defined in json to be implicitly ignoired,
        if (!isActivated &&
                activateOnState == GameWorld.getInstance().getRoundActiveState()) {

            isActivated = true;
            onActivate(ee);
        }

        if (null != collisionProcessor) {

            if (collisionProcessor.processCollision(ee)) {

                onProcessedCollision(ee);
            }
        }
    }

    /*
     * default collision processing handler
     * Spawns a new static mesh shape (and triggers itself for deletion)
     * most subs should override as this is probably NOT what is desired for most situations!
     */
    public void onProcessedCollision(Entity ee) {

// the passed FA is obviously self-reference 'this' ... but note that when the object is built, Nothing
// of this FA attributes is persisted! // Just the way it is ;)
// ... what the so-called User Data is for.

// userData = ?  ... is a java object, cast to anything ... so  is there any sensible default ?

        final String tmpObjectName = "sphere";

        Vector3 translation = new Vector3(); // tmp for new vector instance .. only need to feed the GC relavitvely few of thsesei guess
        ee.getComponent(ModelComponent.class).modelInst.transform.getTranslation(translation);

        CompCommon.spawnNewGameObject(
                new Vector3(1, 1, 1),
                translation,
                this,  // pass-thru
                tmpObjectName);

        ee.add(new StatusComponent(0)); // delete me!
    }


    @Override
    public void onActivate(Entity ee) {
/*
        if (null != collisionProcessor) // .... hmmm let's see
            CompCommon.entityAddPhysicsBody(ee, vT0);
*/
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
