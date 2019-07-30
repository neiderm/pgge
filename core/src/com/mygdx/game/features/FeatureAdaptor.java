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

public class FeatureAdaptor implements FeatureIntrf {

    protected Entity target;

    public boolean inverted;

        // generic integer attributes  ? e.g min/max etc. idfk ...  non-POJO types must be new'd if instantiate by JSON
//    public Vector3 vR = new Vector3();
    public Vector3 vS = new Vector3();
    public Vector3 vT = new Vector3();

    // origins or other gameObject.instance specific data - position, scale etc.
//    public Vector3 vR0 = new Vector3();
//    public Vector3 vS0 = new Vector3();
    public Vector3 vT0 = new Vector3();     // starting Origin (translation) of the entity from the instance data

    /* 
     * for sensors and similar - need a means to assign parameters outside of no-argument constructor (loading classes from JSON)
     * sub-classes may override this and use params at their discrection. 
     */
    public void setTarget(Entity target, Vector3 v3, boolean flag){ /* mt */
//        setTarget(target);
//        this.inverted = flag;
    }

//    private void setTarget(Entity target){
//        this.target = target;
//    }
//
//    // have to poll the exit sensor
//    public boolean _getIsTriggered() {
//        return false;
//    }

    @Override
    public void update(Entity e) { // mt
    }
}
