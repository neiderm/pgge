/*
 * Copyright (c) 2021 Glenn Neidermeier
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
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.FeatureComponent;
import com.mygdx.game.components.ModelComponent;

public class SlideBox extends FeatureAdaptor {

    private final Vector3 vFwrdUnitRay = new Vector3();
    private final Vector3 position = new Vector3();
    private final Vector3 vCollisionRay = new Vector3();
    private float fImpactEnergy;
    private final Vector3 tmpScaleStep = new Vector3();

    private BulletComponent bc;
    private ModelInstance instance;

    @Override
    public void update(Entity ee) {

        super.update(ee);

        if (null == bc) {
            bc = ee.getComponent(BulletComponent.class);

            if (null != bc && null != bc.body) {
                // re   "BulletWorld.addBodyWithCollisionNotif()"
                bc.body.setContactCallbackFlag(BulletWorld.OBJECT_FLAG);
                bc.body.setContactCallbackFilter(BulletWorld.OBJECT_FLAG);
            }
            // (re)set a collision processor (could have different CP than what is from JSON ...eg. a "fall to the floor" CP replaced by the "bump from other object" CP ???
            if (null == collisionProcessor) {
                collisionProcessor = new DebouncedCollisionProc(0);
            }

            instance = ee.getComponent(ModelComponent.class).modelInst;
        }

        /*
         * apply arbitrary threshold to cut off simulated force once "friction" has applied enough deceleration
         */
        btCollisionObject rayPickObject =
                BulletWorld.getInstance().rayTest(
                        instance.transform.getTranslation(position),
                        vCollisionRay, 1.0f);

        if (null == rayPickObject) {
            //  moves by one step vector if there is no collision imminent
            final float FF = 0.040f;
            final float TH = 0.003f;
            fImpactEnergy *= (1.0f - FF); // arbitrary simulated frictional force consumes impact energy until object comes to rest

            if (Math.abs(fImpactEnergy) > TH) {
                final float VFSCALER = 0.1f;
                tmpScaleStep.set(vFwrdUnitRay).scl(VFSCALER);

                instance.transform.trn(tmpScaleStep.scl(fImpactEnergy));

                if (null != bc) {
                    bc.body.setWorldTransform(instance.transform); // sync body to visual model
                } else {
                    Gdx.app.log("SlideBox", "Bullet Component can't be NULL");
                }
            }
        } else {
            Entity target = BulletWorld.getInstance().getCollisionEntity(rayPickObject.getUserValue());

            if (null != target) {
                FeatureComponent tfc = target.getComponent(FeatureComponent.class);

                if (null != tfc) {
                    FeatureAdaptor fa = tfc.featureAdpt;

                    if (null != fa && F_SUB_TYPE_T.FT_SLIDEY_BLK == fa.fSubType) {
                        // only a slidebox target should be handled this way!
                        SlideBox sbTarget = (SlideBox) fa;
                        float frictionLoss = 1.0f - fImpactEnergy;
                        frictionLoss /= 2.0f; // fudge the value until block hits next one
                        float transferredEnergy = 1.0f - frictionLoss;

                        // target feature adapter gets a reference to its own entity .. thats the way it works
                        sbTarget.handleCollision(target, ee, transferredEnergy);
                    }
                }
            }
            vFwrdUnitRay.setZero();
            fImpactEnergy = 0;
        }
    }


    private final Vector3 tmpV3 = new Vector3();

    @Override
    public void onProcessedCollision(Entity ee) {

        if (null != bc) {
            /*
             * get position of colliding object  and determine the direction/vector of the collision -
             */
            DebouncedCollisionProc dcp = (DebouncedCollisionProc) collisionProcessor;
            Entity collisionObject = dcp.collisionObject;

            if (null != collisionObject) {

                handleCollision(ee, collisionObject, 1 /* energy always one here */);
            }
        }
    }


    void handleCollision(Entity ee, Entity collisionObject, float energy) {

        if (null == bc) {

            bc = ee.getComponent(BulletComponent.class);
        }
        /*
         * get position of colliding object  and determine the direction/vector of the collision -
         */
        if (null != bc && null != collisionObject) {

            ModelComponent mc = collisionObject.getComponent(ModelComponent.class);

            if (null != mc && null != mc.modelInst.transform /* && null != mc.modelInst */) {

                mc.modelInst.transform.getTranslation(tmpV3);   // collision object position

                Matrix4 xfm = bc.body.getWorldTransform();

                if (null != xfm) {
                    xfm.getTranslation(vFwrdUnitRay);
                }

                vFwrdUnitRay.sub(tmpV3);
                vFwrdUnitRay.y = 0;
                float absDX = Math.abs(vFwrdUnitRay.x);
                float absDZ = Math.abs(vFwrdUnitRay.z);

                // snap the collision force vector to "N, S, E, W"
                if (absDX > absDZ) {
                    vFwrdUnitRay.z = 0;
                    //if (absDX > 0)
                    {
                        vFwrdUnitRay.x = vFwrdUnitRay.x / absDX; // negated
                    }
                } else {
                    vFwrdUnitRay.x = 0;
                    //if (absDX > 0)
                    {
                        vFwrdUnitRay.z = vFwrdUnitRay.z / absDZ;
                    }
                }

                fImpactEnergy = energy; // initial collision "energy" starts at 100%

                final float HalfExtent = 1; // box is 2 x 2 x 2
                vCollisionRay.set(vFwrdUnitRay).scl(HalfExtent); // fudged, needs to be half-extent  of objecgt dimension axis of collision
            }
        }
    }
}
