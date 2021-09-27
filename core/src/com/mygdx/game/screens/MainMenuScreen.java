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
import com.mygdx.game.sceneLoader.GameFeature;

/**
 * Created by neiderm on 12/18/17.
 * <p>
 * Reference:
 * on-screen menus:
 * https://www.gamedevelopment.blog/full-libgdx-game-tutorial-menu-control/
 * UI skin defined programmatically:
 * https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/UISimpleTest.java
 */
public class MainMenuScreen implements Screen {
    /*
     * extend Stage in order to override keyDown?
     */
    private final InGameMenu stage = new InGameMenu();

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
        Gdx.gl.glClearColor(66.0f / 255, 66.0f / 255, 231.0f / 255, 1.f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        stage.act();
        stage.draw();

        int idxCurSel = stage.setCheckedBox();

        if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_A)) {
            // pass along the local player object name
            GameFeature gf =
                    GameWorld.getInstance().getFeature(GameWorld.LOCAL_PLAYER_FNAME);

            String featureName = null;

            if (null != gf) {
                featureName = gf.getObjectName();
            }
            switch (idxCurSel) {
                default:
                case 0:
                    // set scene for Bonus Level
                    GameWorld.getInstance().setSceneData("GameData.json", featureName);
                    GameWorld.getInstance().showScreen(new LoadingScreen(/* should_pause=true, type=Level */));
                    break;
                case 1:
                    GameWorld.getInstance().showScreen(); // show the default screen
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
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.addToggleButton("Bonus Level");
        stage.addToggleButton("Exit");
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

    @Override
    public void dispose() {
        stage.dispose();
    }
}
