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
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.sceneLoader.GameFeature;
import com.mygdx.game.util.ModelInstanceEx;

/**
 * Created by neiderm on 7/5/2019.
 * <p>
 * This can be a "generic" handler for a sensor. assigned a single target Entity to be sensing for.
 */

public class OmniSensor extends FeatureAdaptor {

    protected Entity target;
    boolean inverted;
    boolean isTriggered;

    private Vector3 sensorOrigin = new Vector3(); // the reference point for determining an object has exitted the level
    private Vector3 bounds = new Vector3();
    private Vector3 tgtPosition = new Vector3();

    private Vector3 omniRadius = new Vector3();

    private final Vector3 DEFAULT_RADIUS = new Vector3(1.5f, 1.5f, 1.5f); //

    private Ray lookRay = new Ray();
    private Vector3 direction = new Vector3();// new Vector3(0, 0, -1); // vehicle forward ... whatever, just another working vector instance
    private Quaternion rotation = new Quaternion();

    float senseZoneDistance = 5.0f;


    public OmniSensor() {/* no-arg constructor */

        omniRadius.set(DEFAULT_RADIUS); // maybe .. idfk
    }

    @Override
    public void init(Object target) {

//        super.init(target); // not much there, just sets the target,
        this.target = (Entity) target;

        this.omniRadius.set(vS);

        // vector sensor offset
        senseZoneDistance = vR.x;
    }


    @Override
    public void update(Entity sensor) {

        super.update(sensor);

////        sensorOrigin.set(vT0);  // grab the starting Origin (translation) of the entity from the instance data
// hmmmm ...  it could have been spawned "up there" and now falling to the floor ... so vT0 as handed by GameObject constructor is not the origin we're looking for!

        ModelComponent mymc = sensor.getComponent(ModelComponent.class);

        if (null != mymc) {

            Matrix4 sensTransform = mymc.modelInst.transform;
            sensorOrigin = sensTransform.getTranslation(sensorOrigin);

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
            omniRadius.set(mymc.boundingRadius, 0, 0).add(vS.x, 0, 0);

            // sensor origin is offset on a vector ray cast in relative forward facing direction

            lookRay.set(sensTransform.getTranslation(sensorOrigin), // myPos
                    ModelInstanceEx.rotateRad(direction.set(0, 0, -1), sensTransform.getRotation(rotation)));

        } else {
// hack for a non-graphical sensor
            sensorOrigin.set(vT0);  // grab the starting Origin (translation) of the entity from the instance data

            omniRadius.set(vS.x, 0, 0);

            // sensor origin is offset on a vector ray cast in relative forward facing direction
//            rotation = new Quaternion();
            lookRay.set(sensorOrigin,
                    ModelInstanceEx.rotateRad(direction.set(0, 0, -1), rotation.set(0, 0, 0, 1)));
        }

        /*
The distance between two points in a three dimensional - 3D - coordinate system can be calculated as

d = ((x2 - x1)^2 + (y2 - y1)^2 + (z2 - z1)^2)^(1/2)

https://www.engineeringtoolbox.com/distance-relationship-between-two-points-d_1854.html

but for this purpose square roots don't need to be taken ... first get reference distance as distance
from point of sensor origin to a any point lying on the sphere of given radius
omni radius not being used consistently (sometimes just x given, some times xyz ... doesn't matter, it
 ends up just an (essentially arbitrary ) scalar float anyway
 */

        /* add scaled look-ray-unit-vector to sensor position */
        sensorOrigin.add(lookRay.direction.scl(senseZoneDistance)); // we'll see


        bounds.set(sensorOrigin);
        bounds.add(omniRadius);
        float boundsDst2 = bounds.dst2(sensorOrigin);

        if (null == target) {

            GameFeature playerFeature = GameWorld.getInstance().getFeature("Player");
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

            updateTriggered(sensor, isTriggered);
        }
    }


    private int bucket;

    private void updateTriggered(Entity sensor, boolean triggered) {

        KillSensor.ImpactType impactType = this.impactType;

//        if (null != impactType)
        {
            if (triggered) {
                if (bucket < 1) {

                    if (null != impactType) {

                        // clock target probly for player, other wise probly no status comp
                        StatusComponent sc = target.getComponent(StatusComponent.class);

                        if (KillSensor.ImpactType.ACQUIRE == impactType) {

                            if (null != sc) {
                                sc.prizeCount += 1;
                            }
                            // use sensor model instance texture etc. idfk
                            KillSensor.makeBurnOut(
                                    sensor.getComponent(ModelComponent.class).modelInst,
                                    KillSensor.ImpactType.ACQUIRE // impactType
                            );

                            sensor.add(new StatusComponent(0)); // delete me! ... 0 points bounty

                        } else {

                            int lc = 0;
                            if (null != sc) {

                                if (sc.lifeClock > 0) {
                                    sc.lifeClock -= 10;
                                }
                                if (KillSensor.ImpactType.FATAL == impactType) {
                                    sc.lifeClock = 0;
                                }
                                lc = sc.lifeClock;
                            }

                            if (lc <= 0) {
                                impactType = KillSensor.ImpactType.FATAL;
                            }
                            // use the target model instance texture etc.
                            KillSensor.makeBurnOut(
                                    target.getComponent(ModelComponent.class).modelInst, impactType);
                        }
                    }
                }
                bucket += 1;

            } else {
                bucket -= 2;
                if (bucket < 0) {
                    bucket = 0;
                }
            }
        }
    }
}
