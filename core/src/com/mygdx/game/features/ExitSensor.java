/*
 * Copyright (c) 2021 Glenn Neidermeier
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
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.util.ModelInstanceEx;

@SuppressWarnings("unused")
public class ExitSensor extends OmniSensor {

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        if (isActivated && isTriggered) {

            StatusComponent sc = target.getComponent(StatusComponent.class);
            sc.canExit = true;
        }
    }

    /*
     * TBD ... late adding physics body? not sure what purpose
     * WIP experiment with "dynamic" i.e. no mesh geometry or physics characteristics are given in the
     * json, only the feature (Exit SEnsor)
     *  a Collision Processor class is designated ... which here is the cue to arbitrarily assigns a physics
     * body, setting it at VT0 as usual .. Vt0 assigned from vT since there is no usual body instance translation available
     * Intend to generalize but for now it was cluttering up the feature/sensor class hierarchy
     * Also experimented with assigning position relative to target/player whatever reason
     *
     * specified debouncedCollisionProc and as noted here is given a mesh and physics body
     * default onProcessedCollision() is in featureAdaptor and simply spawns a new object defaulty body "sphere"
     */
    @Override
    public void onActivate(Entity sensor) {
        // vT0 is set to a body position if one was given (otherwise 0'd) otherwise vT may be set
        // so by adding 3rd possibility is vT0 of a body is offset by vT on activation ... fwr
        vT0.add(vT);

        if (null != collisionProcessor) {// .... hmmm let's see, apparently this should involve collisions
            CompCommon.entityAddPhysicsBody(sensor, vT0); // for now this is only a boring old box shape!
        }

        ModelComponent mc = sensor.getComponent(ModelComponent.class);

        if (null != mc && null != mc.modelInst && mc.modelInst.materials.size > 0) {
            ModelInstanceEx.setColorAttribute(mc.modelInst, new Color(Color.OLIVE)); // tmp test code
        }
    }

    /*
     * if it has a physics body, it can be called on collision handler
     * Spawns a new static mesh shape (and triggers itself for deletion)
     */
    @Override
    public void onProcessedCollision(Entity sensor) {

        final String tmpObjectName = "cone";

        CompCommon.spawnNewGameObject(
                new Vector3(1, 1, 1), sensorOrigin,
                new ExitSensor(),
                tmpObjectName);

        sensor.add(new StatusComponent(0)); // delete me!
    }
}
