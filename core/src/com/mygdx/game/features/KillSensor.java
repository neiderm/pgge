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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;

/**
 * Created by neiderm on 7/5/2019.
 * <p>
 */
public class KillSensor extends OmniSensor {

    int lifeClock = 0;         // default is terminate this Projectile immed.

    private int bucket;

    // working variables
    private final Vector3 tmpV = new Vector3();
    private final Quaternion rotation = new Quaternion();

    public enum ImpactType {
        NONE,
        FATAL,
        DAMAGING,
        STRIKE,
        ACQUIRE,
        POWERUP
    }

    // needs to put a #of_rounds on the power-up weapon

    /*
     * Projectile needs to call a different constructor to inform for its explosion handling ...
     */


    public KillSensor() {

        impactType = ImpactType.DAMAGING; // can be set in scene file
    }

    public KillSensor(Entity target) {

        this();

        this.target = target;

        // proj. sense radius (provde constructor arg)
        this.vS.set(1.5f, 0, 0); // vS.x + projectile_radius = radiys of the kill sensor
    }

//    /*
//     * doesn't do much but get a vector for the shooters forwared-orientation and scale to projectile movement delta vector
//     */
//    // probably get rid of this one
//    private Vector3 getDirectionVector(Matrix4 shootersTransform) {
//
//        Vector3 vvv = new Vector3();
//
//        shootersTransform.getRotation(orientation);
//
//        // set unit vector for direction of travel for theoretical projectile fired perfectly in forwared direction
//        float mag = -0.15f; // scale the '-1' accordingly for magnitifdue of forward "velocity"
//
//        vvv.set(ModelInstanceEx.rotateRad(tmpV.set(0, 0, mag), orientation));
//
//        return vvv;
//    }


    @Override
    public void update(Entity sensor) {

        sensor.getComponent(StatusComponent.class).lifeClock = this.lifeClock;

        // updates the damage status on the target
        super.update(sensor);

        updateTriggered(sensor, isTriggered, sensorOrigin);

        int lc =         sensor.getComponent(StatusComponent.class).lifeClock;
        if (!isTriggered && 0 == lc ){
            // kill sensor is probbly created by a e.g. a projectile and must be immediately disposed i.e. probabably not a character
// make a Strike impact
            ModelInstance smi = sensor.getComponent(ModelComponent.class).modelInst;
            KillSensor.makeBurnOut(smi, KillSensor.ImpactType.STRIKE);
        }
    }


    private void updateTriggered(Entity sensor, boolean triggered, Vector3 sensorPos) {

        if (triggered) {
            if (bucket < 1) {
//                if (null != this.impactType) //
                {
                    updateImpact(this.impactType, sensor, sensorPos);
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

    private void updateImpact(KillSensor.ImpactType impactT, Entity sensor, Vector3 sensorPos) {

        StatusComponent tsc = target.getComponent(StatusComponent.class);
// try not to shoot down the walls
// etc. ... BurnOut blue hit-ring
        if (null != tsc) {

            if (KillSensor.ImpactType.ACQUIRE == impactT) {

                tsc.prizeCount += 1;

                ModelInstance senorModelInst = null;

                ModelComponent smc = sensor.getComponent(ModelComponent.class);
                if (null != smc) {
                    senorModelInst = smc.modelInst;
                }

                // use sensor model instance texture etc. idfk
                if (null != senorModelInst) {
                    KillSensor.makeBurnOut(senorModelInst, KillSensor.ImpactType.ACQUIRE);
                }

                sensor.add(new StatusComponent(0)); // delete me! ... 0 points bounty

            } else if (KillSensor.ImpactType.DAMAGING == impactT
                    || KillSensor.ImpactType.FATAL == impactT) {
                // use the target model instance texture etc.
                ModelInstance tmi = target.getComponent(ModelComponent.class).modelInst;
// if (null != tmi
                tmi.transform.getRotation(rotation); // reuse tmp rotation variable
                tmpV.set(0, -1, 0); //  2.5d simplification
                float orientationAngle = rotation.getAngleAround(tmpV);

                float hitAngle = angleDetermination(
                        sensorPos, tmi.transform.getTranslation(tmpV), orientationAngle);

                int n = (int) (Math.round(hitAngle / 90) + 0.5f);
                if (n >= 4) {
                    n -= 4;
                }
                tsc.damage[n] += 100 / 5; // damage/shield levels are 0-100

                // if the shield damage @ 100%, then set impact type to Fatal
                if (tsc.damage[n] >= 100) {
                    tsc.lifeClock = 0;
                }
                if (tsc.lifeClock > 0) {
                    tsc.lifeClock -= 1;
                }
                if (tsc.lifeClock <= 0) {
                    impactT = KillSensor.ImpactType.FATAL;
                }

                KillSensor.makeBurnOut(tmi, impactT); // use target model instance for burn out texture
            }

            else {// else impactT ?

                System.out.print("anybody home?");//                KillSensor.makeBurnOut(null, KillSensor.ImpactType.STRIKE);
            }
        }
    }

    private static float angleDetermination(Vector3 sensorPos, Vector3 targetPos, float orientationAngle) {

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

    /*
     */
    private static void makeBurnOut(ModelInstance mi, ImpactType impactT) {

        TextureAttribute fxTextureAttrib = null;

        Vector3 translation = new Vector3(); // tmp for new vector instance .. only need to feed the GC relavitvely few of thsesei guess

        Vector3 scale = new Vector3(1, 1, 1);
        if (ImpactType.STRIKE == impactT){
            scale.set(0.5f, 0.5f, 0.5f);
        }

        if (null != mi) {
            Material saveMat = mi.materials.get(0);

            TextureAttribute tmpTa = (TextureAttribute) saveMat.get(TextureAttribute.Diffuse);

            if (null != tmpTa) {
                Texture tt = tmpTa.textureDescription.texture;
                fxTextureAttrib = TextureAttribute.createDiffuse(tt);

//            fxTextureAttrib = (TextureAttribute)tmpTa.copy(); // idfk maybe toodo
            }

            translation = mi.transform.getTranslation(translation);
        }

        CompCommon.spawnNewGameObject(scale, translation,
                new BurnOut(fxTextureAttrib, impactT), "sphere");
    }
}
