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

package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.mygdx.game.GameWorld;
import com.mygdx.game.sceneLoader.GameFeature;

/**
 * Created by neiderm on 12/18/17.
 */

/*
 * Reference:
 *  on-screen menus:
 *   https://www.gamedevelopment.blog/full-libgdx-game-tutorial-menu-control/
 *  UI skin defined programmatically:
 *   https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/UISimpleTest.java
 */

public class MainMenuScreen implements Screen {

    private InGameMenu stage; // I think we need to extend stage (like did for GamePad) in order to Override keyDown


    MainMenuScreen() { // mt
    }

    @Override
    public void render(float delta) {

//        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(66.0f / 255, 66.0f / 255, 231.0f / 255, 1.f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        stage.act();
        stage.draw();

        int idxCurSel = stage.checkedUpDown(stage.mapper.getDpad(null).getY());
        stage.setCheckedBox(idxCurSel);

        if (InputMapper.InputState.INP_FIRE1 == stage.mapper.getInputState()) {
            // pass along the local player object name
            GameFeature gf =
                    GameWorld.getInstance().getFeature( /* SceneData.LOCAL_PLAYER_FNAME */ "Player");

            String featureName = null;

            if (null != gf) {
                featureName = gf.getObjectName(); // i hope so
            }

            switch (idxCurSel) {
                default:
                case 0:
                    GameWorld.getInstance().setSceneData("GameData.json", featureName);
                    GameWorld.getInstance().showScreen(new LoadingScreen());
                    break;

                case 1:
                    GameWorld.getInstance().setSceneData("SelectScreen.json"); // maybe
                    GameWorld.getInstance().showScreen( /* ScreenEnum screenEnum, Object... params */
                            new LoadingScreen(false, LoadingScreen.ScreenTypes.SETUP));
                    break;

                case 2:
                    GameWorld.getInstance().showScreen(new SplashScreen());
                    break;
            }
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

        stage = new InGameMenu("skin/uiskin.json", null);
        Gdx.input.setInputProcessor(stage);

        stage.addButton("New Game", "toggle");
        stage.addButton("Preferences", "toggle");
        stage.addButton("Exit", "toggle");

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
