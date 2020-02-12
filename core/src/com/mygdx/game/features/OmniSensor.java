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
import com.badlogic.gdx.graphics.g3d.ModelInstance;
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

    private ModelComponent mymc;

    float senseZoneDistance = 5.0f;


    public OmniSensor() {/* no-arg constructor */

        omniRadius.set(DEFAULT_RADIUS); // maybe .. idfk
    }

    @Override
    public void init(Object target) {

//        super.init(target); // not much there, just sets the target,
        this.target = (Entity) target;

//        this.omniRadius.set(vS);

        // vector sensor offset
        senseZoneDistance = vR.x;

        // grab the starting Origin (translation) of the entity (vT0 set from instance data)
        sensorOrigin.set(vT0);
    }


    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        omniRadius.set(vS.x, 0, 0); // if this is model-based entity, bounding radius is added to omni radius below

        if (null == mymc) {

            mymc = sensor.getComponent(ModelComponent.class);

            // grab the starting Origin (translation) of the entity (vT0 set from instance data)
//            sensorOrigin.set(vT0);
        } else {

            Matrix4 sensTransform = mymc.modelInst.transform;

            // if it has a valid model comp and transform, then update the vT0 position from the model instance
            vT0.set(sensTransform.getTranslation(vT0));

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

            // sensor origin is offset on a vector ray cast in relative forward facing direction
            lookRay.set(sensTransform.getTranslation(sensorOrigin),
                    ModelInstanceEx.rotateRad(direction.set(0, 0, -1), sensTransform.getRotation(rotation)));

            if (senseZoneDistance > 0) {
                /* add scaled look-ray-unit-vector to sensor position */
                sensorOrigin.add(lookRay.direction.scl(senseZoneDistance)); // we'll see
            }
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


    private Vector3 tmpV = new Vector3();
    private Vector3 sensorPos = new Vector3();
    private Vector3 targetPos = new Vector3();
    private int bucket;

    private void updateTriggered(Entity sensor, boolean triggered) {

        KillSensor.ImpactType impactType = this.impactType;

//        if (null != impactType)
        {
            if (triggered) {
                if (bucket < 1) {

                    if (null != impactType) {

                        sensorPos.set(vT0); // fallback for sensor position if no model instance

                        ModelInstance senorModelInst = null;

                        ModelComponent smc = sensor.getComponent(ModelComponent.class);
                        if (null != smc) {
                            senorModelInst = smc.modelInst;
                            sensorPos = smc.modelInst.transform.getTranslation(sensorPos);
                        }

                        // clock target probly for player, other wise probly no status comp
                        StatusComponent tsc = target.getComponent(StatusComponent.class);

                        if (null == tsc) {
                            tsc = new StatusComponent(); // entity doesn't have an SC .. just make a dummy one to keep below logic cleaner
                        }
//                        if (null != tsc)
                        {
                            if (KillSensor.ImpactType.ACQUIRE == impactType) {

                                tsc.prizeCount += 1;

                                // use sensor model instance texture etc. idfk
                                if (null != senorModelInst) {
                                    KillSensor.makeBurnOut(senorModelInst, KillSensor.ImpactType.ACQUIRE);
                                }

                                sensor.add(new StatusComponent(0)); // delete me! ... 0 points bounty

                            } else { // damaging or fatal
                                // use the target model instance texture etc.
                                ModelInstance tmi = target.getComponent(ModelComponent.class).modelInst;

                                if (tsc.lifeClock > 0) {
                                    tsc.lifeClock -= 1;
                                }
                                if (tsc.lifeClock <= 0) {
                                    impactType = KillSensor.ImpactType.FATAL;
                                }
// if (null != tmi
                                tmi.transform.getRotation(rotation); // reuse tmp rotation variable
                                tmpV.set(0, -1, 0); //  2.5d simplification
                                float orientationAngle = rotation.getAngleAround(tmpV);

                                float hitAngle = angleDetermination(
                                        sensorPos, tmi.transform.getTranslation(targetPos), orientationAngle);

                                int n = (int) (Math.round(hitAngle / 90) + 0.5f);
                                if (n >= 4) {
                                    n -= 4;
                                }
                                tsc.damage[n] += 100 / 5; // damage/shield levels are 0-100

                                KillSensor.makeBurnOut(tmi, impactType);
                            }
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


    private float angleDetermination(Vector3 sensorPos, Vector3 targetPos, float orientationAngle) {

        float dX = sensorPos.x - targetPos.x;
        float dZ = sensorPos.z - targetPos.z;

        float angle;
        // flips the sign to make the angle to be that of the target relative to the  projectile
        angle = (float) Math.atan(dX / dZ) * 180f / (float) Math.PI;
        angle *= -1;
/*
as ABS(x) approaches PI, z is approaching 0 as tan(x/z) approaches the poles at -PI and +PI
 */
        if (dZ < 0) {
            angle = 180 + angle;
//            System.out.println("< " + angle);
        }
//        else if (dZ > 0){
////            System.out.println("> " + angle);
//        }
//        else {
//        // tangent undefined when num==0, so angle is either 90 or (-90)
//            if (dX < 0) {
//                angle = 360 - 90;
//            }
//            else if (dX > 0) {
//                angle = 0 + 90;
//            }
//        }

        // reciprocal of angle i.e angle from target to project
        angle = angle + 180;

        angle -= orientationAngle;

        // angle is now rotated to be relative to orientation of target!
        // just need to take it in 0-359 degrees range
        if (angle < 0) {
            angle += 360f;
        } else if (angle >= 360) {
            angle -= 360f;
        }

//        System.out.println("angle= " + angle);
        return angle;
    }
}
