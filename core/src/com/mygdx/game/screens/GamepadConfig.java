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
package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.mygdx.game.GameWorld;

/**
 * Created by neiderm on 12/07/19.
 * Very crude controllers config, WIP
 */
public class GamepadConfig implements Screen {

    private InGameMenu stage; // extend stage (like did for GamePad) in order to Override keyDown?

    GamepadConfig() { // mt
    }

    @Override
    public void render(float delta) {
//        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(66.0f / 255, 66.0f / 255, 231.0f / 255, 1.f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        stage.act();
        stage.draw();

        int idxCurSel = stage.setCheckedBox();

        if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_A)
                || (0 != stage.mapper.getAxisI(InputMapper.VIRTUAL_AD_AXIS)) // analog stick-right, hackamathang
        ) {
            GameWorld.getInstance().setControllerMode(idxCurSel);
            GameWorld.getInstance().setSceneData("SelectScreen.json"); // maybe
            GameWorld.getInstance().showScreen( /* ScreenEnum screenEnum, Object... params */
                    new LoadingScreen(false, LoadingScreen.ScreenTypes.SETUP));
        }
    }

    @Override
    public void resize(int width, int height) {
          /*
    https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
    We need to update the stage's viewport in the resize method. The last Boolean argument set the origin to the lower left coordinate, causing the label to be drawn at that location.
     */
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void show() {
        final String bType = "toggle";
        stage = new InGameMenu();
        Gdx.input.setInputProcessor(stage);

        stage.addButton("Microsoft X-Box 360 pad (Linux:USB)", bType);
        InputMapper.numberControlCfgTypes++;
        stage.addButton("Xbox 360 Controller (Win 2.4G or USB)", bType);
        InputMapper.numberControlCfgTypes++;
        stage.addButton("Android B/T", bType);
        InputMapper.numberControlCfgTypes++;
        stage.addButton("n45 Dual Analog Gamepad (USB)", bType); // IOS Linux (Windows apparently has stick axes reversed)
        InputMapper.numberControlCfgTypes++;
        stage.addNextButton();
    }

    @Override
    public void pause() {        // empty
    }

    @Override
    public void resume() {        // empty
    }

    @Override
    public void hide() {
        dispose();
    }
}
