/*
 * Copyright (c) 2022 Glenn Neidermeier
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
package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.components.BulletComponent;

/**
 * Created by neiderm on 12/18/17.
 */

public class BulletSystem extends IteratingSystem {

    public BulletSystem() {
        super(Family.all(BulletComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) { // empty
    }

    @Override
    public void addedToEngine(Engine engine) {

        super.addedToEngine(engine);

        engine.addEntityListener(Family.all(BulletComponent.class).get(), listener);
    }

    private EntityListener listener = new EntityListener() {
        @Override
        public void entityAdded(Entity entity) { // MT
        }

        @Override
        public void entityRemoved(Entity entity) {
            BulletComponent bc = entity.getComponent(BulletComponent.class);
            if (null != bc) {
                // assert null != bc.shape
                // assert null != bc.body
                BulletWorld.getInstance().removeBody(bc.body);

                if (null != bc.motionstate) {
                    bc.motionstate.dispose();
                }
                bc.shape.dispose();
                bc.body.dispose();
//                bc.body = null; // idfk ... is this useful?
            }
        }
    };
}
