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
package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.GameWorld;

/**
 *
 */
public class SplashScreen extends BaseScreenWithAssetsEngine {

    private InGameMenu stage; // don't instantiate me here ... skips select platform
    private SpriteBatch batch;
    private Texture ttrSplash;
    private int timer;

    private static final int MIN_SP_SCREEN_TIME = (2 * 60);
    private static final int MAX_SP_SCREEN_TIME = (6 * 60);

    public SplashScreen() {
        timer = 0; //stay on splash screen for minimum time
    }

    private boolean wasTouched;

    @Override
    public void render(float delta) {
        // plots debug graphics
        super.render(delta);

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        Gdx.gl.glViewport(0, 0, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(ttrSplash, 0, 0, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
        batch.end();

        // allow detected TS input to advance past splash screen once minimum time elapses
        if (!wasTouched) {
            wasTouched = Gdx.input.isTouched(0);
        }

        boolean isA = (wasTouched || stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_A));

        if ((timer > MAX_SP_SCREEN_TIME) || ((timer > MIN_SP_SCREEN_TIME) && isA)) {

            GameWorld.getInstance().showScreen();

        } else {
            timer += 1;
        }
    }

    @Override
    public void hide() {  // mt
        dispose();
    }

    @Override
    public void pause() {  // mt
    }

    @Override
    public void resume() {  // mt
    }

    @Override
    public void show() {
        super.init();
        stage = new InGameMenu();
        Gdx.input.setInputProcessor(stage);

        batch = new SpriteBatch();
        ttrSplash = new Texture("splash-screen.png");
    }

    @Override
    public void resize(int width, int height) {  // mt
    }

    @Override
    public void dispose() {
        ttrSplash.dispose();
        batch.dispose();
        stage.dispose();
        // screens that load assets must calls assetLoader.dispose() !
        super.dispose();
    }
}
