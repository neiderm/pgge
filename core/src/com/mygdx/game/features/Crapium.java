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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.sceneLoader.SceneLoader;
import com.mygdx.game.util.ModelInstanceEx;

/*
 * crap you have to pick up
 */
public class Crapium extends OmniSensor {

    private Attribute saveAttribute;


    @Override
    public void init(Object obj) {

        super.init(obj);

        // ha nice hackage
        SceneLoader.numberOfCrapiums += 1;
    }


    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        ModelComponent mc = sensor.getComponent(ModelComponent.class);
        ModelInstance modelInst = mc.modelInst;

        updatePlatformRotation(modelInst.transform);

        Material mat = null;
        if (modelInst.materials.size > 0) {
            mat = modelInst.materials.get(0);

//        if (null == mat)
//            return; // throw new GdxRuntimeException("not found");
        }

        if (isTriggered) {

            if (null == saveAttribute) {

                if (null != mat) {
                    // grab the color attribute
                    saveAttribute = mat.get(ColorAttribute.Diffuse);
                }
            }

            ModelInstanceEx.setColorAttribute(
                    modelInst, new Color(0.1f, 0.2f, 0.3f, 0.4f)); // tmp test code

            sensor.add(new StatusComponent(0)); // delete me! ... 0 points bounty

            CompCommon.makeBurnOut(modelInst, CompCommon.ImpactType.ACQUIRE);

            StatusComponent sc = target.getComponent(StatusComponent.class);

            if (null != sc) {
                sc.prizeCount += 1;
            }
        }
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
        tmpV.set(0, 0, 1); // todo: get the actual "down" vector e.g. in case on inclined sfc.
        float orientationAngle = orientation.getAngleAround(tmpV);

        if (orientationAngle > rotationMax) {
            rotationStep = -ROTATION_STEP_DEGREES;

        } else if (orientationAngle < rotationMin) {
            rotationStep = ROTATION_STEP_DEGREES;
        }

        myxfm.rotate(tmpV, rotationStep);
    }
}
