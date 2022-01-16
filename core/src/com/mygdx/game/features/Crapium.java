/*
 * Copyright (c) 2021-2022 Glenn Neidermeier
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
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.sceneloader.GameObject;
import com.mygdx.game.sceneloader.InstanceData;
import com.mygdx.game.sceneloader.SceneLoader;

/*
 * crap you have to pick up
 */
public class Crapium extends KillSensor {

    public static final int BOUNTY_CTAINER = 10000;
    public static final int BOUNTY_POWERUP = 20000;

    private Attribute saveAttribute;
    private StatusComponent sc;
    @SuppressWarnings("unused")
    private Vector3 rotationAxis; // in JSON, don't delete

    private Crapium() {
        this.lifeClock = 1; // because base uddate sets this, to 0
        this.vS.set(1.5f, 0, 0);
    }

    private Crapium(ImpactType impactType) {
        this();
        this.impactType = impactType;
    }

    @Override
    public void init(Object obj) {

        super.init(obj);

        if (ImpactType.ACQUIRE == impactType) {
            // ha nice hackage
            SceneLoader.incNumberOfCrapiums();
        }
    }

    @Override
    public void update(Entity sensor) {

        super.update(sensor);
        // Copy Bounty over to StatusComp so the killSensor dispatch can get it - put this kludgey crap in KillSensor?
        if (null == sc) {
            sc = sensor.getComponent(StatusComponent.class);
        }

        ModelComponent mc = sensor.getComponent(ModelComponent.class);
        ModelInstance modelInst = mc.modelInst;

        updatePlatformRotation(modelInst.transform);

        Material mat = null;

        if (modelInst.materials.size > 0) {
            mat = modelInst.materials.get(0);
        }

        if (isTriggered) {

            if (null == saveAttribute && null != mat) {
                // grab the color attribute
                saveAttribute = mat.get(ColorAttribute.Diffuse);
            }

            if (ImpactType.POWERUP == impactType) {
                // Only allow the bounty.powerup to register if this crapium has been collided-with
                // (triggered) and not by projectile impact.
                this.bounty = (BOUNTY_POWERUP + 1); // flag this as a powerup to the post-frame cleaner

                if (null != sc) {
                    sc.lifeClock = 0; // Powerup is acquired, kill off the powerup crapium
                }
            }
        }
    }

    @Override
    public void onDestroyed(Entity e) {

        ModelComponent mc = e.getComponent(ModelComponent.class);// could cache this model comp lookup?
// if (null != fmc)
        mc.modelInst.transform.getTranslation(tmpV);

        // spawn the new prize pickup
        if (impactType == ImpactType.CONTAINER) {

            Crapium powerup = new Crapium(ImpactType.POWERUP);
            powerup.fSubType = fSubType;  // assign powerup type from the destroyed container

            // Insert a newly created game object into the "spawning" model group. Must set the
            // powerup to pickable to make it shootable - if shot, powerup is destroyed!
            GameObject gameObject = new GameObject("cylinder", 0,
                    new Vector3(0.98f, 0.98f, 0.98f),
                    new InstanceData(powerup,
                            mc.modelInst.transform.getTranslation(tmpV)));
            // flags the entity if it does not emit a BurnOut if destroyed (powerup)
            gameObject.isPickable = true;
            gameObject.iSWhatever = true; // isNoBurnout

            GameWorld.getInstance().addSpawner(gameObject);
        }
        // else if (impactType == ImpactType.POWERUP) {
    }

    private static final float ROTATION_STEP_DEGREES = 0.5f;
    private static final float ROTATION_RANGE_DEGREES = 90.0f;
    private Quaternion orientation = new Quaternion();
    private Vector3 tmpV = new Vector3();

    private void updatePlatformRotation(Matrix4 myxfm) {

        myxfm.getRotation(orientation);
        tmpV.set(1, 0, 1);

        if (rotationAxis != null) {
            tmpV.set(rotationAxis);
        }
        myxfm.rotate(tmpV, ROTATION_STEP_DEGREES);
    }
}
