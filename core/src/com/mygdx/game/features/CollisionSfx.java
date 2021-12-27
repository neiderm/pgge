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
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.GameWorld;

public class CollisionSfx extends DebouncedCollisionProc {

    private String key;
    private Vector3 slocation;

    public CollisionSfx(String key, Vector3 slocation) {
        this.key = key;
        this.slocation = new Vector3(slocation);
    }

    @Override
    public void onCollision(Entity collisionObject) {

        super.onCollision(collisionObject);

        final Sound sfx0 = GameWorld.AudioManager.getSound(key);

        GameWorld.AudioManager.playSound(sfx0, slocation);
    }
}
