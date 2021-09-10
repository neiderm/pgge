/*
 * Copyright (c) 2021 Glenn Neidermeier
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

public class MyGdxGame extends Game {

    @Override
    public void create() {
        Gdx.input.setCatchBackKey(true);
        GameWorld.getInstance().initialize(this);
    }

    @Override
    public void dispose() {
        // Make Game World disposable ... Note this only seems to be incurred in Desktop lwjgl environment (Alt+F4).
        // ( btw Screen:dispose() is not called automatically by framework )
        GameWorld.getInstance().dispose();  // dispose the current screen
    }
}
