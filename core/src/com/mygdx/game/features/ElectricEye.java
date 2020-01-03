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
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.util.ModelInstanceEx;

/*
 * original prototype for killin things
 */
public class ElectricEye extends VectorSensor {

    @Override
    public void update(Entity sensor) {

        super.update(sensor);


        ModelComponent mc = sensor.getComponent(ModelComponent.class); // can this be cached?
// if (null != mc)
        if (mc.modelInst.materials.size > 0) {

            ModelInstanceEx.setColorAttribute(mc.modelInst, new Color(Color.ROYAL)); // tmp test code
        }
//else
//    Gdx.app.log("sdf", "sdf"); //  doesn't necessarily have a material

        if (isTriggered) {

            if (mc.modelInst.materials.size > 0) {

                ModelInstanceEx.setColorAttribute(mc.modelInst, new Color(Color.GOLD)); // tmp test code
            }
        }
    }
}
