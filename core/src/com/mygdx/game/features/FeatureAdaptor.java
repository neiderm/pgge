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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.sceneLoader.GameObject;
import com.mygdx.game.sceneLoader.InstanceData;

public class FeatureAdaptor implements FeatureIntrf {

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


    private int tempasdf;

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
     */
    public void onProcessedCollision(Entity ee) {

//        CompCommon.mkStaticFromDynamicEntity(ee);

        spawnNewGameObject(ee);
        ee.add(new StatusComponent(true)); // delete me!
    }

    /*
     * clone gaame objectfeature ?
     */
    private void spawnNewGameObject(Entity ee) {

        // insert a newly created game OBject into the "spawning" model group
        GameObject gameObject = new GameObject();
        gameObject.isShadowed = true;

        Vector3 size = new Vector3(0.5f, 0.5f, 0.5f); /// size of the "box" in json .... irhnfi  bah
        gameObject.scale = new Vector3(size);

//        gameObject.mass = 1; // let it be stationary

        gameObject.objectName = "sphere";

        Vector3 translation = new Vector3();
//                translation = bc.body.getWorldTransform().getTranslation(translation);

        ModelComponent mc = ee.getComponent(ModelComponent.class);

        Matrix4 tmpM4 = mc.modelInst.transform;
        translation = tmpM4.getTranslation(translation);

        InstanceData id = new InstanceData(translation);


        FeatureAdaptor es = getFeatureAdapter(this); // clone the feature
        es.isActivated = true; // force activation set ???

        es.init(ee);
        es.vS.set(new Vector3(1.5f, 0, 0));
        id.adaptr = es;
        gameObject.getInstanceData().add(id);

        GameWorld.getInstance().addSpawner(gameObject); // toooodllly dooodddd    object is added "kinematic" ???
    }


    @Override
    public void onActivate(Entity ee) {
        /*
         * vT = positoin ! this is the default activation for now, otherwise override!
         */
        if (null != collisionProcessor) // .... hmmm let's see
            CompCommon.entityAddPhysicsBody(ee, vT0);
    }

    @Override
    public void onCollision(Entity e, int id) { // mt
    }

    /*
     * returns a new instance of featureAdapter
     * The JSON read creates a new instance when sceneData is built, but we want to create a new instance
     * each time to be sure all data is initialized this is only being used for type information ... it
     * is instanced in SceneeData but the idea is for each game Object (Entity) to have it's own feature
     * adatpr instance
     */
    public FeatureAdaptor makeFeatureAdapter(Vector3 position, Entity target) {

        FeatureAdaptor adaptor = getFeatureAdapter(this);

        if (null != adaptor) {

            // maybe this is lame but have to copy each field of interest .. (clone () ??
            adaptor.activateOnState = this.activateOnState;

            if (null == adaptor.activateOnState) {
                adaptor.isActivated = true; // default to "activated" if no activation trigger is specified
            } else { // idfk ...
                adaptor.isActivated = this.isActivated; // activation flag can be set (independent of activation state)
            }

            adaptor.collisionProcessor = this.collisionProcessor;

            // argument passing convention for model instance is vT, vR, vS (trans, rot., scale) but these can be anything the sub-class wants.
            // get the "characteristiics" for this type from the JSON
//                        adaptor.vR.set(fa.vR);
            adaptor.vS.set(vS);
            adaptor.vT.set(vT);

            // get location or whatever from object instance data
//                        adaptor.vR0.set(0, 0, 0); // unused ... whatever
//                        adaptor.vS0.set(transform.getScale(tmpV));

            // grab the starting Origin (translation) of the entity from the instance data
            adaptor.vT0.set(position);

/* initting all these at once then eh
            adaptor.init(target);
            */
        }

        return adaptor;
    }

    static FeatureAdaptor getFeatureAdapter(FeatureAdaptor thisFa) {

        FeatureAdaptor adaptor = null;

        Class c = thisFa.getClass();

        try {
            adaptor = (FeatureAdaptor) c.newInstance(); // have to cast this ... can cast to the base-class and it will still take the one of the intended sub-class!!

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return adaptor;
    }
}
