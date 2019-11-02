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
import com.mygdx.game.components.ModelComponent;

public class ShootamaThing extends ElectricEye {

    private Quaternion orientation = new Quaternion();
    private Vector3 down = new Vector3();
    private Vector3 xlation = new Vector3();
    private float platformDegrees;
    private float rotationStep = 0.25f;
    private final float rotationMin = 0;
    private final float rotationMax = 90;

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        /*
        toodoo use raycast to determine dx to stop of cast and if target comes within x degrees of
        whatever direction it happens to be pointing, then it begins rotating to track target

        turn toward the target

        simple rotation to start
         */

        ModelComponent tmc = target.getComponent(ModelComponent.class);
        Matrix4 txfm = tmc.modelInst.transform;

        txfm.getRotation(orientation);

        ModelComponent mymc = sensor.getComponent(ModelComponent.class);
        Matrix4 myxfm = mymc.modelInst.transform;

        xlation = myxfm.getTranslation(new Vector3(xlation));
        myxfm.setToRotation(down.set(0, 1, 0), platformDegrees);
        myxfm.setTranslation(new Vector3(xlation));

        platformDegrees += rotationStep; // we'll see

        if (platformDegrees > rotationMax) {
            platformDegrees = rotationMax;
            rotationStep *= -1;
        } else if (platformDegrees < rotationMin) {
            platformDegrees = rotationMin;
            rotationStep *= -1;
        }
    }
}
