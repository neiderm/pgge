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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.FeatureComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;

public class Projectile extends FeatureAdaptor {

    // working variables
    private final Vector3 tmpV = new Vector3();
    private final Vector3 vF = new Vector3();


    public Projectile(Vector3 vFp) {

        this.vF.set(vFp);
    }

    public Projectile(Vector3 vFp, F_SUB_TYPE_T fSubType) {

        this(vFp);
        this.fSubType = fSubType;
    }


    @Override
    public void update(Entity projectile) {

        Vector3 position = tmpV;

        // projectile moves by one step vector if there is no collision imminent
        ModelComponent fmc = projectile.getComponent(ModelComponent.class);// could cache this model comp lookup?
// if (null != fmc)
        fmc.modelInst.transform.getTranslation(position);
        btCollisionObject rayPickObject = BulletWorld.getInstance().rayTest(position, vF, 1.0f);

        if (null == rayPickObject) {
            // no collision imminient so keep it moving along
            fmc.modelInst.transform.trn(vF);
        } else {
            // stopped projectile
            StatusComponent sc = projectile.getComponent(StatusComponent.class);
            if (null != sc) {
                sc.lifeClock = 0;    // kill off the projectile
            } else {
                projectile.add(new StatusComponent(0));
            }

            // projectile should make a ringy thing if hit wall or should shatter into triangles if impact on a shootable
// unfortunately it seems walls are reported by the getCollisnEntity  as valid target Entity   blah
            Entity target = BulletWorld.getInstance().getCollisionEntity(rayPickObject.getUserValue());

            if (null != target) {
                FeatureComponent tfc = target.getComponent(FeatureComponent.class);

                F_SUB_TYPE_T targetType = F_SUB_TYPE_T.FT_NONE;

                if (null != tfc) {
                    FeatureAdaptor fa = tfc.featureAdpt;
                    if (null != fa) {

                        fa.bounty = 0; // no bounty for you

                        targetType = fa.fSubType;
                    }
                }

                if (F_SUB_TYPE_T.FT_RESERVED == targetType) {
                    // special sauce for the SliderBOx and ExitBox - they are physics entities and get flagged as "shootable" but must definately not be!
                    target = null; //register the impact (show blue puff) but definately don't kill the target!
                }

                // spawn the sensor to handle the outcome
                CompCommon.spawnNewGameObject(
                        new Vector3(0.1f, 0.1f, 0.1f),
                        position,   //                                mi.transform.getTranslation(trans),
                        new KillSensor(target),
                        "capsule"); // make this non-graphical ... the specified mesh-shape shown only momentairly
            }
        }
    }
}
