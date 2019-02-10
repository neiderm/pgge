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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.GameWorld;
import com.mygdx.game.SceneLoader;

import static com.mygdx.game.screens.LoadingScreen.ScreenTypes.LEVEL;

/**
 * Created by neiderm on 7/16/2018.
 * based on:
 * http://www.pixnbgames.com/blog/libgdx/how-to-make-a-splash-screen-in-libgdx/?_sm_nck=1
 */

public class LoadingScreen implements Screen {

    private ShapeRenderer shapeRenderer = new ShapeRenderer();

    private SpriteBatch batch;
    private Texture ttrSplash;

    private int loadCounter = 0;
    private boolean isLoaded;
    private boolean shouldPause = true;
    private ScreenTypes screenType = LEVEL;
    private InputMapper mapper;


    public enum ScreenTypes {
        SETUP,
        LEVEL
    }

    LoadingScreen(String path, boolean shouldPause, ScreenTypes screenType) {
        this(path);
        this.shouldPause = shouldPause;
        this.screenType = screenType;
    }

    LoadingScreen(String path) {

        batch = new SpriteBatch();
        ttrSplash = new Texture("data/crate.png");

        font = new BitmapFont(
                Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);
        font.getData().setScale(1.0f);

        isLoaded = false;

        // hmm yep hackish awright
        if (null != GameWorld.sceneLoader) {
            GameWorld.sceneLoader.dispose();
            GameWorld.sceneLoader = null;
        }

        GameWorld.sceneLoader = new SceneLoader(path);  // bah

        mapper = new InputMapper();
    }

    private void loadNewScreen() {

        switch (screenType) {
            default:
            case LEVEL:
                GameWorld.getInstance().showScreen(new GameScreen());
                break;
            case SETUP:
                GameWorld.getInstance().showScreen(new SelectScreen());
                break;
        }
    }

    private StringBuilder stringBuilder = new StringBuilder();
    private BitmapFont font;

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(ttrSplash, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        font.draw(batch, stringBuilder,
                Gdx.graphics.getWidth() / 4f, (Gdx.graphics.getHeight() / 4f) * 3f);
        batch.end();


        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(new Color(255, 0 , 0, 1));
        shapeRenderer.rect(
                (Gdx.graphics.getWidth() / 4f) , (Gdx.graphics.getHeight() / 2f) - 5,
                20f + loadCounter, 10);

        shapeRenderer.end();

        /*
         * make sure loadNewScreen() not called until rendering pass ... hide() destroys everything!
         */
        if (!isLoaded) {

            stringBuilder.setLength(0);
            stringBuilder.append("Loading ... ");

            // make the bar up to half the screen width
            loadCounter = 
               (int)(Gdx.graphics.getWidth() * 0.5f * GameWorld.sceneLoader.getAssets().getProgress()) ;

            if (GameWorld.sceneLoader.getAssets().update()) {
                GameWorld.sceneLoader.doneLoading();
                isLoaded = true;
            }
        } else {

            stringBuilder.setLength(0);
            stringBuilder.append("Ready!");

            // simple polling for a tap on the touch screen
            if (InputMapper.InputState.INP_SELECT == mapper.getInputState(true) ||  !shouldPause) {

                loadNewScreen();
            }
        }

    }

    @Override
    public void hide() {

        dispose();
    }

    @Override
    public void pause() {        // mt
    }

    @Override
    public void resume() {        // mt
    }

    @Override
    public void show() {        // mt
    }

    @Override
    public void resize(int width, int height) {        // mt
    }

    @Override
    public void dispose() {
        ttrSplash.dispose();
        batch.dispose();
        shapeRenderer.dispose();
//        sceneLoader.dispose();   //  no  dont be silly ...
    }
}
