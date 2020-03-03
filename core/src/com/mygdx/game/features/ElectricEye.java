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
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.screens.GfxBatch;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;

/*
 * original prototype for killin things
 * It's just a kill sensor with an offset and a line .
 */
public class ElectricEye extends KillSensor {

    private final Vector3 forwardV = new Vector3(0, 0, -1); // vehicle forward
    private Vector3 startPoint = new Vector3();
    private Vector3 endPoint = new Vector3();
    private Ray lookRay = new Ray();
    private Vector3 direction = new Vector3(0, 0, -1); // vehicle forward
    private Quaternion rotation = new Quaternion();
    private GfxUtil lineInstance;


    public ElectricEye(){
        this.lifeClock = 1;  // because base uddate sets this, to 0
    }

    @Override
    public void init(Object userData) {

        super.init(userData);

        // don't construct Gfx Util in the constructor if wanting to check the json file writer ... all the line model instance stuff shows up in a model group
        this.lineInstance = new GfxUtil();
    }

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


        endPoint.set(sensTransform.getTranslation(startPoint));

        lookRay.set(endPoint,
                ModelInstanceEx.rotateRad(direction.set(forwardV), sensTransform.getRotation(rotation)));

        /* add scaled look-ray-unit-vector to sensor position */
        endPoint.add(lookRay.direction.scl(senseZoneDistance)); // we'll see

        GfxBatch.draw(lineInstance.lineTo(startPoint, endPoint, Color.SALMON));
    }
}
