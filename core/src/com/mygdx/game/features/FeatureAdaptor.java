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

    protected Object userData; // oh geeez ... this is arbitrary data is populated into the "cloned'd" FA ...

    private GameWorld.GAME_STATE_T activateOnState;
    protected boolean isActivated;

    public CollisionProcessorIntrf collisionProcessor;

    // generic integer attributes  ? e.g min/max etc. idfk ...  non-POJO types must be new'd if instantiate by JSON
//    public Vector3 vR = new Vector3();
    public Vector3 vS = new Vector3();
    public Vector3 vT = new Vector3();

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
        if (null != activateOnState) {
            if (
                    !isActivated &&
                            activateOnState == GameWorld.getInstance().getRoundActiveState()) {

                isActivated = true;

                onActivate(ee);
            }
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

        String tmpObjectName = "sphere";

        CompCommon.spawnNewGameObject(
                ee.getComponent(ModelComponent.class).modelInst, this, tmpObjectName);

        ee.add(new StatusComponent(true)); // delete me!
    }


    @Override
    public void onActivate(Entity ee) {
        /*
         * vT = positoin ! this is the default activation for now, otherwise override!
         */
        if (null != collisionProcessor) // .... hmmm let's see
            CompCommon.entityAddPhysicsBody(ee, vT0);
    }


    /*
     * returns a new instance of featureAdapter - i think it was in part to separate the runtime from the (de)serialized (JSON)  in Scene Data
     * The JSON read creates a new instance when sceneData is built, but we want to create a new instance
     * each time to be sure all data is initialized this is only being used for type information ... it
     * is instanced in SceneeData but the idea is for each game Object (Entity) to have it's own feature
     * adatpr instance
     */
    public FeatureAdaptor makeFeatureAdapter(Vector3 position, Entity unused_i_guess) {

        FeatureAdaptor adaptor = cpyFeatureAdapter(this);

        if (null != adaptor) {

            // maybe this is lame but have to copy each field of interest .. (clone () ??
            adaptor.activateOnState = this.activateOnState;

            if (null == adaptor.activateOnState) {
                adaptor.isActivated = true; // default to "activated" if no activation trigger is specified
            } else { // idfk ...
                adaptor.isActivated = this.isActivated; // activation flag can be set (independent of activation state)
            }

            adaptor.collisionProcessor = cpyColllisionProcessor(this.collisionProcessor); // this.collisionProcessor;

            // argument passing convention for model instance is vT, vR, vS (trans, rot., scale) but these can be anything the sub-class wants.
            // get the "characteristiics" for this type from the JSON
//                        adaptor.vR.set(fa.vR);
            adaptor.vS.set(vS);
            adaptor.vT.set(vT);

            // get location or whatever from object instance data
//                        adaptor.vR0.set(0, 0, 0); // unused ... whatever
//                        adaptor.vS0.set(transform.getScale(tmpV));

// hope init() won't clobber  vt0
            // grab the starting Origin (translation) of the entity from the instance data
            adaptor.vT0.set(position);

// big hack ... idfk... need some kind of generic means to let the Feature Adapter sub-class take care of its derived implementation
            adaptor.init(userData);
        }

        return adaptor;
    }

    /*
     here is some nice hackery to get an instance of the type of sub-class ...constructor of
     sub-class is invoked but that's about it ... far from beging much of an actual "clone" at this point
     */
    private static FeatureAdaptor cpyFeatureAdapter(FeatureAdaptor thisFa) {

        FeatureAdaptor adaptor = null;

        Class c = thisFa.getClass();

        try {
            adaptor = (FeatureAdaptor) c.newInstance(); // have to cast this ... can cast to the base-class and it will still take the one of the intended sub-class!!

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return adaptor;
    }
// obviously this cpy paste search replace
    private static CollisionProcessorIntrf cpyColllisionProcessor(CollisionProcessorIntrf cpi) {

        CollisionProcessorIntrf cpy = null;
if (null != cpi) {
    Class c = cpi.getClass();

    try {
        cpy = (CollisionProcessorIntrf) c.newInstance(); // have to cast this ... can cast to the base-class and it will still take the one of the intended sub-class!!

    } catch (Exception ex) {

        ex.printStackTrace();
    }
}
        return cpy;
    }
}
