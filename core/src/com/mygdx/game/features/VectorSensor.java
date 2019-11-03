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
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.sceneLoader.GameFeature;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;

/**
 * Created by neiderm on 7/5/2019.
 * <p>
 * sensor for close proximity in one direction to a target
 */

public class VectorSensor extends SensorAdaptor {

    private Vector3 trans = new Vector3();
    private GfxUtil lineInstance;// = new GfxUtil();

    private Vector3 myPos = new Vector3();
    private Vector3 targetPos = new Vector3();
    private Ray lookRay = new Ray();
    private Vector3 direction = new Vector3(0, 0, -1); // vehicle forward
    private Quaternion rotation = new Quaternion();

    // tmp  need to be Game Data
    private float senseZoneRadius = 2.0f;
    private float senseZoneDistance = 5.0f;


    public VectorSensor() {/*mt*/}

    @Override
    public void init(Object target){

        super.init(target);

// we'll see
        senseZoneRadius = vS.x;
        senseZoneDistance = vT.x;
    }


    @Override
    public void update(Entity sensor) {

        //        super.update(me);

        /*
         * note duplicate the target-getting as from omni sensor
         */
        if (null == target) {

            GameFeature playerFeature = GameWorld.getInstance().getFeature("Player");
            if (null != playerFeature) {

                //// ha ha hackit hgacktity
                target = playerFeature.getEntity();
            }
        } else {

            Matrix4 plyrTransform = target.getComponent(ModelComponent.class).modelInst.transform;
            targetPos = plyrTransform.getTranslation(targetPos);

            Matrix4 sensTransform = sensor.getComponent(ModelComponent.class).modelInst.transform;
            myPos = sensTransform.getTranslation(myPos);

            lookRay.set(sensTransform.getTranslation(myPos), // myPos
                    ModelInstanceEx.rotateRad(direction.set(0, 0, -1), sensTransform.getRotation(rotation)));

            /* add scaled look-ray-unit-vector to sensor position */
            myPos.add(lookRay.direction.scl(senseZoneDistance)); // we'll see

// the Feature Adapter will be populated in the "out.json" file but don't want the debug line instance
// spewing all it's crap in there, so allow the debug line graphic to be instantantiated late
///*
            if (null == lineInstance) {
                lineInstance = new GfxUtil();
            }
            RenderSystem.debugGraphics.add(lineInstance.lineTo(
                    sensTransform.getTranslation(trans),
                    myPos,
                    Color.SALMON));
//*/
            // take differnece from  center-of-sense-zone-sphere to target.
            // If that distance fall within the sense radius.
            myPos.sub(targetPos);

            if (Math.abs(myPos.x) < senseZoneRadius && Math.abs(myPos.z) < senseZoneRadius) {
                isTriggered = true;
            }
        }
    }
}
