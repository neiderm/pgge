/*
 * Copyright (c) 2020 Glenn Neidermeier
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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.util.ModelInstanceEx;

/*
 * based on "Projectile" class from 0b99298 prior to cutover to Buleet-based collision
 * Projectile is set with fixed target from GameEvent notfier (the green-line thingy)
 */
public class SensProjectile extends KillSensor {

    // working variables
    private final Vector3 tmpV = new Vector3();
    private final Vector3 vF = new Vector3();

    public SensProjectile() {

        impactType = KillSensor.ImpactType.DAMAGING; // can be set in scene file

        isActivated = true;

        this.lifeClock = 1;  // because base uddate sets this, to 0
    }

    public SensProjectile(Entity target, Vector3 vFp) {

        this();

        this.target = target;

        // proj. sense radius (provde constructor arg)
        this.vS.set(1, 0, 0); // vS.x + projectile_radius = radiys of the kill sensor

        this.vF.set(vFp);
    }


    @Override
    public void update(Entity projectile) {

        super.update(projectile);

        // projectile moves by one step vector if there is no collision imminent
        ModelComponent fmc = projectile.getComponent(ModelComponent.class);// could cache this model comp lookup?
// if (null != fmc)
        fmc.modelInst.transform.getTranslation(tmpV);
        btCollisionObject rayPickObject = BulletWorld.getInstance().rayTest(tmpV, vF, 1.0f);

        if (null == rayPickObject) {
            // no collision imminient so keep it moving along
            fmc.modelInst.transform.trn(vF);
        } else {
// stopped projectile
            if (fmc.modelInst.materials.size > 0) {
                ModelInstanceEx.setColorAttribute(fmc.modelInst, new Color(Color.PINK), 0.5f); //tmp?
            }
        }

        if (isTriggered && isActivated) {

            isActivated = false; // don't let it "detonate" again!

            // simple obvious action is to terminate this Projectile
            if (null != projectile.getComponent(StatusComponent.class)) {
                this.lifeClock = 0;                                                      // blah hacky crap
                projectile.getComponent(StatusComponent.class).lifeClock = 0; // termintate me
            } else {
                projectile.add(new StatusComponent(0)); // be careful not to overwrite!
            }
        }
    }
}
