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
import com.badlogic.gdx.audio.Music;
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

    private String featureName;
    private Music bgMusic;

    MainMenuScreen() {

        bgMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/Track_15.ogg"));

        // get the local player object name from previous screen scene data
        GameFeature gf = GameWorld.getInstance().getFeature(GameWorld.LOCAL_PLAYER_FNAME);

        if (null != gf) {
            featureName = gf.getObjectName();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
        Gdx.gl.glClearColor(0, 0, 0, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        stage.act();
        stage.draw();

        int idxCurSel = stage.updateMenuSelection();

        if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_A)) {

            if (0 == idxCurSel) {
                // set scene for Bonus Level
                GameWorld.getInstance().showScreen(featureName);
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
        stage.createMenu("Arena Completed", "Score");
        if (null != bgMusic){
            bgMusic.setLooping(true);
            bgMusic.play();
        }
    }

    @Override
    public void pause() {        // empty
//        bgMusic.pause(); // todo try android hiding
    }

    @Override
    public void resume() {        // empty
//        bgMusic.play(); // // todo try android hiding
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (null != bgMusic){
            bgMusic.dispose();
        }
        stage.dispose();
    }
}
