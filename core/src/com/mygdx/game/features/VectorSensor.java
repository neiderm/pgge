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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.screens.GfxBatch;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;

/**
 * Created by neiderm on 7/5/2019.
 * <p>
 * sensor for close proximity in one direction to a target
 */
public class VectorSensor extends OmniSensor {

    private Vector3 trans = new Vector3();
    private GfxUtil lineInstance;
    private Vector3 myPos = new Vector3();
    private Ray lookRay = new Ray();
    private Vector3 direction = new Vector3(0, 0, -1); // vehicle forward
    private Quaternion rotation = new Quaternion();

    @Override
    public void init(Object userData) {

        super.init(userData);

        // don't construct Gfx Util in the constructor if wanting to check the json file writer ... all the line model instance stuff shows up in a model group
        this.lineInstance = new GfxUtil();
    }

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        Matrix4 sensTransform = sensor.getComponent(ModelComponent.class).modelInst.transform;
        myPos.set(sensTransform.getTranslation(trans));

        lookRay.set(myPos,
                ModelInstanceEx.rotateRad(direction.set(0, 0, -1), sensTransform.getRotation(rotation)));

        /* add scaled look-ray-unit-vector to sensor position */
        myPos.add(lookRay.direction.scl(senseZoneDistance)); // we'll see

        GfxBatch.draw(lineInstance.lineTo(trans, myPos, Color.SALMON));
    }
}
