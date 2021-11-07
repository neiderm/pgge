/*
 * Copyright (c) 2021 Glenn Neidermeier
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
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.sceneLoader.GameFeature;
import com.mygdx.game.util.ModelInstanceEx;

/**
 * Created by neiderm on 7/5/2019.
 * This can be a "generic" handler for a sensor. assigned a single target Entity to be sensing for.
 */
public class OmniSensor extends FeatureAdaptor {

    private final Vector3 bounds = new Vector3();
    private final Vector3 direction = new Vector3();
    private final Vector3 omniRadius = new Vector3();
    private final Ray lookRay = new Ray();
    private final Quaternion rotation = new Quaternion();

    private Vector3 tgtPosition = new Vector3();
    private ModelComponent mymc;

    protected Entity target;

    final Vector3 sensorOrigin = new Vector3(); // the reference point for determining an object has exitted the level

    boolean inverted = false;
    boolean isTriggered;
    float senseZoneDistance = 5.0f;
    KillSensor.ImpactType impactType = KillSensor.ImpactType.NONE;
    Matrix4 sensTransform;

    OmniSensor() {/* no-arg constructor */
        Vector3 DEFAULT_RADIUS = new Vector3(1.5f, 1.5f, 1.5f);
        omniRadius.set(DEFAULT_RADIUS);
    }

    @Override
    public void init(Object target) {

        if (null != target) {
            this.target = (Entity) target;
        }
        // vector sensor offset
        senseZoneDistance = vR.x;
        // grab the starting Origin (translation) of the entity (vT0 set from instance data)
        sensorOrigin.set(vT0);
        // in case this is a non-model entity, set transform translation at least initialize to vT0
        sensTransform = new Matrix4(); // construct this here so that it doesn't get set in the .json test writer which is harmless but annoying
        sensTransform.setTranslation(sensorOrigin);
    }


    @Override
    public void update(Entity sensor) {

        super.update(sensor);
        omniRadius.set(vS.x, 0, 0); // if this is model-based entity, bounding radius is added to omni radius below

        if (null == mymc) {
            mymc = sensor.getComponent(ModelComponent.class);
        } else {
            sensTransform = mymc.modelInst.transform;

//        if (mymc.boundingRadius > 0 && vS.x == 0) {
//            // calc bound radius e.g. sphere will be larger than actual as it is based on dimensions
//            // of extents (box) so in many cases will look not close enuff ... but brute force
//            // collsision detect based on center-to-center dx of objects so that about as good as it
//            // gets (troubel detect collision w/  "longer" object )
//            omniRadius.set(mymc.boundingRadius, 0, 0);
//        }
/*
 default bounding radius determined from mesh dimensions (if there is mesh geometry assoc. w/
 this sensor feature)... optional specify  vS  to be added to br such that allowing the effective
 radius to be any arbitryariy sized irrespective of mesh size
*/
            omniRadius.add(mymc.boundingRadius, 0, 0);
        }

        // if it has a valid model comp and transform, then update the vT0 position from the model instance
        sensTransform.getTranslation(sensorOrigin);

        // sensor origin is offset on a vector ray cast in relative forward facing direction
        lookRay.set(sensorOrigin,
                ModelInstanceEx.rotateRad(direction.set(0, 0, -1), sensTransform.getRotation(rotation)));

        if (senseZoneDistance > 0) {
            /* add scaled look-ray-unit-vector to sensor position */
            sensorOrigin.add(lookRay.direction.scl(senseZoneDistance)); // we'll see
        }

/*
 The distance between two points in a three dimensional - 3D - coordinate system can be calculated as

  d = ((x2 - x1)^2 + (y2 - y1)^2 + (z2 - z1)^2)^(1/2)

  https://www.engineeringtoolbox.com/distance-relationship-between-two-points-d_1854.html

 but for this purpose square roots don't need to be taken ... first get reference distance as distance
 from point of sensor origin to a any point lying on the sphere of given radius.
 Omni radius not being used consistently (sometimes just x given, some times xyz ... doesn't matter, it
 ends up just an (essentially arbitrary ) scalar float anyway
*/

        bounds.set(sensorOrigin);
        bounds.add(omniRadius);
        float boundsDst2 = bounds.dst2(sensorOrigin);

        if (null == target) {
            GameFeature playerFeature = GameWorld.getInstance().getFeature(GameWorld.LOCAL_PLAYER_FNAME);
            if (null != playerFeature) {

                target = playerFeature.getEntity();
            }
        } else {
            ModelComponent tmc = target.getComponent(ModelComponent.class);

            if (null != tmc) {
                Matrix4 tgtTransform = tmc.modelInst.transform;

                if (null != tgtTransform) {
                    tgtPosition = tgtTransform.getTranslation(tgtPosition);
                }

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
