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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.sceneLoader.GameFeature;

/**
 * Created by neiderm on 7/5/2019.
 * <p>
 * This can be a "generic" handler for a sensor. assigned a single target Entity to be sensing for.
 */

public class OmniSensor extends SensorAdaptor {

    private Vector3 sensorOrigin = new Vector3(); // the reference point for determining an object has exitted the level
    private Vector3 bounds = new Vector3();
    private Vector3 tgtPosition = new Vector3();

    Vector3 omniRadius = new Vector3();

    private final Vector3 DEFAULT_RADIUS = new Vector3(1.5f, 1.5f, 1.5f); //


    public OmniSensor() {/* no-arg constructor */

        omniRadius.set(DEFAULT_RADIUS); // maybe .. idfk
    }

    @Override
    public void init(Object target) {

        super.init(target); // not much there, just sets the target,

        this.omniRadius.set(vS);
/*
seems useless right now ....
        this.sensorOrigin.set(vT);
*/
    }

    private Vector3 vvv = new Vector3();

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        // grab the starting Origin (translation) of the entity from the instance data
// hmmmm ...  it could have been spawned "up there" and now falling to the floor ... so vT0 as handed by GameObject constructor is not the origin we're looking for!

////        sensorOrigin.set(vT0);  // grab the starting Origin (translation) of the entity from the instance data

        ModelComponent _mc = sensor.getComponent(ModelComponent.class);
        Matrix4 transform = _mc.modelInst.transform;
        vvv = transform.getTranslation(vvv);
        sensorOrigin.set(vvv);               ///   blah


        bounds.set(sensorOrigin);
        bounds.add(omniRadius);

        float boundsDst2 = bounds.dst2(sensorOrigin);

        if (null == target) {

            GameFeature playerFeature = GameWorld.getInstance().getFeature("Player");
            if (null != playerFeature) {

                //// ha ha hackit hgacktity
                target = playerFeature.getEntity();
            }
        } else {
            ModelComponent mc = target.getComponent(ModelComponent.class);
            if (null != mc) {
                Matrix4 tgtTransform = mc.modelInst.transform;

                if (null != tgtTransform)
                    tgtPosition = tgtTransform.getTranslation(tgtPosition);


                isTriggered = false; // hmmm should be "non-latching? "

                if (inverted) {
                    if (tgtPosition.dst2(sensorOrigin) > boundsDst2) {
                        isTriggered = true;
                    }
                } else {
                    if (tgtPosition.dst2(sensorOrigin) < boundsDst2) {
                        isTriggered = true;
                    }
                }
            }
        }
    }
}
