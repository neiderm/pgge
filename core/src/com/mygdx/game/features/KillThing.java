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
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.util.ModelInstanceEx;

/*
 * ....Feature  "DroppedBody" ..  .......... becomes un-dynamic
 * Presently, this one  strips off the physics body, it does NOT respawn as a new entity.
 */
public class KillThing extends KillSensor {

    public KillThing(){
        this.lifeClock = 1;  // because base uddate sets this, to 0
    }

    @Override
    public void onProcessedCollision(Entity ee){

        // toooooooooodooo ... can it have option when to activate or detonate (bomb vs. mine?)

//        super.onProcessedCollision(ee);
        CompCommon.physicsBodyMarkForRemoval(ee);

        ModelComponent mc = ee.getComponent(ModelComponent.class);
        ModelInstance mi = mc.modelInst;
        if (mi.materials.size > 0) {
            ModelInstanceEx.setColorAttribute(mi, new Color(Color.RED), 0.9f); //tmp?
        }
    }
}
