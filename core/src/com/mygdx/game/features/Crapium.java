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
import com.mygdx.game.sceneLoader.GameObject;
import com.mygdx.game.sceneLoader.InstanceData;
import com.mygdx.game.sceneLoader.SceneLoader;

/*
 * crap you have to pick up
 */
public class Crapium extends KillSensor {

    public static final int BOUNTY_CTAINER = 10000;
    public static final int BOUNTY_POWERUP = 20000;

    private Vector3 rotationAxis; // in JSON, don't delete

    private Attribute saveAttribute;

    private StatusComponent sc;

    public Crapium() {

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
            SceneLoader.numberOfCrapiums += 1;
        }
    }


    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        // put this kludgey crap in KillSensor ... ?   copy Bounty over to StatusComp so the killSensor dispatch can get it
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

            if (null == saveAttribute) {

                if (null != mat) {
                    // grab the color attribute
                    saveAttribute = mat.get(ColorAttribute.Diffuse);
                }
            }

            if (ImpactType.POWERUP == impactType) {
// only allow the bounty.powerup to register if this crapium has been collided-with (triggered) and not by projectile impact
                this.bounty = (BOUNTY_POWERUP + 1); // flag this as a powerup to the post-frame cleaner

                if (null != sc) {
                    sc.lifeClock = 0; // PowerUP is acquired, kill off the powerup crapium
                }
                // should make a purple burnout // ModelInstanceEx.setColorAttribute(modelInst, new Color(0f, 1f, 0f, 0.4f));
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

//            //  // needs to set the powerup to pickable to make it shootable - if shot, powerup is destroyed!
            // insert a newly created game OBject into the "spawning" model group
            GameObject gameObject = new GameObject("cylinder", 0,
                    new Vector3(0.98f, 0.98f, 0.98f),
                    new InstanceData(powerup,
                            mc.modelInst.transform.getTranslation(tmpV)));
            // flags the entity if it does not emit a BurnOut if destroyed (powerup)
            gameObject.isPickable = true;
            gameObject.iSWhatever = true; // isNoBurnout

            GameWorld.getInstance().addSpawner(gameObject);
        }
// else         if (impactType == ImpactType.POWERUP) {
//        }
    }


    private Quaternion orientation = new Quaternion();
    private Vector3 tmpV = new Vector3();
    private final float ROTATION_STEP_DEGREES = 0.5f;
    private float rotationStep = ROTATION_STEP_DEGREES;
    private final float ROTATION_RANGE_DEGREES = 90.0f;
    private float rotationMin = 0;
    private float rotationMax = 0;

    private void updatePlatformRotation(Matrix4 myxfm) {

        myxfm.getRotation(orientation);
        tmpV.set(1, 0, 1);

        if (rotationAxis != null) {
            tmpV.set(rotationAxis);
        }

        myxfm.rotate(tmpV, rotationStep);
    }
}
