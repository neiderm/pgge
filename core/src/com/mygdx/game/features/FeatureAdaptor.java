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
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

public class FeatureAdaptor implements FeatureIntrf {

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
    public void update(Entity e) { // mt
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

        FeatureAdaptor adaptor = null;

        Class c = getClass();

        if (c.toString().contains("KillSensor")) {
            Gdx.app.log("asdf", c.toString()); // tmp
        }

        try {

            adaptor = (FeatureAdaptor) c.newInstance(); // have to cast this ... can cast to the base-class and it will still take the one of the intended sub-class!!

            if (null != adaptor) {
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

                adaptor.init(target);
            }
        } catch (Exception ex) {
            //System.out.println("we're doomed");
            ex.printStackTrace();
        }

        return adaptor;
    }
}
