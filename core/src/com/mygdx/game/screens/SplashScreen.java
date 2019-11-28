/*
 * Copyright (c) 2019 Glenn Neidermeier
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
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.GameWorld;

/**
 * Created by neiderm on 7/16/2018.
 * based on:
 * http://www.pixnbgames.com/blog/libgdx/how-to-make-a-splash-screen-in-libgdx/?_sm_nck=1
 */

public class SplashScreen implements Screen {

    private static String dataFileName = "SelectScreen.json";
    private SpriteBatch batch;
    private Texture ttrSplash;
    private InputMapper mapper;

    public SplashScreen() { // mt
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(ttrSplash, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        // one-time check for TS input (determine wether to show touchpad controll)
        boolean isTouched = Gdx.input.isTouched(0);

        // set global status of touch screen for dynamic configuring of UI on-screen touchpad etc.
        // (but once global "isTouchscreen" is set, don't clear it ;)
        if (!GameWorld.getInstance().getIsTouchScreen() && isTouched)
            GameWorld.getInstance().setIsTouchScreen(true);

        /*
         * make sure loadNewScreen() not called until rendering pass ... hide() destroys everything!
         */
//        if (mapper.checkInputState(InputMapper.InputState.INP_SELECT, true)){   .......... doesn't work for TS :(

//        if (InputMapper.InputState.INP_SELECT == mapper.getInputState(true))
        {
            /*
             * if no data file found, go into test  Screen
             */
            if (Gdx.files.internal(dataFileName).exists()) { // returns void so check forst  valid file path

                GameWorld.getInstance().setSceneData(dataFileName);
                GameWorld.getInstance().showScreen(new LoadingScreen(true, LoadingScreen.ScreenTypes.SETUP));
            } else {
                Gdx.app.log("SplachScfren", "using TestMeScreen");
                GameWorld.getInstance().showScreen(new ReduxScreen());
            }
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
        batch = new SpriteBatch();
        ttrSplash = new Texture("splash-screen.png");
        mapper = new InputMapper();
    }

    @Override
    public void resize(int width, int height) {  // mt
    }

    @Override
    public void dispose() {
        ttrSplash.dispose();
        batch.dispose();
    }
}
