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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.GameWorld;
import com.mygdx.game.sceneLoader.SceneLoader;

/**
 * Created by neiderm on 7/16/2018.
 * based on:
 * http://www.pixnbgames.com/blog/libgdx/how-to-make-a-splash-screen-in-libgdx/?_sm_nck=1
 */
public class LoadingScreen implements Screen {
    // stage is of type inGameMenu in order to have control and keyboard input
    private final InGameMenu stage = new InGameMenu();

    private int loadCounter = 0;
    private int screenTimer = 60 * 3; //fps*sec
    private boolean isLoaded;
    private float alpha = 1.0f;
    private ScreenTypes screenType;
    private Screen newScreen;
    private Texture ttrBackDrop; // reference to selected texture (does not need disposed)

    private static final Color fadeoutColor = new Color(0, 0, 0, 0);
    private static final Color splashBarColor = new Color(Color.BLACK);
    private static final Color loadBarColor = new Color(255, 0, 0, 1);

    // disposables
    private static final Texture ttrSplash = new Texture("splash-screen.png");
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Texture ttrLoad;
    private SpriteBatch spriteBatch = new SpriteBatch();
    private BitmapFont bitmapFont = new BitmapFont(
            Gdx.files.internal(GameWorld.DEFAULT_FONT_FNT),
            Gdx.files.internal(GameWorld.DEFAULT_FONT_PNG), false);

    public enum ScreenTypes {
        SETUP,
        LEVEL
    }

    public LoadingScreen(ScreenTypes screenType) {
        this.screenType = screenType;
    }

    public LoadingScreen() {
        this(ScreenTypes.LEVEL);
    }

    @Override
    public void show() {
        // instancing asset Loader class kicks off asynchronous asset loading which we need to start
        // right now obviously. Then the asset loader instance must be passed off to the Screen to use and dispose.
        switch (screenType) {
            default:
            case LEVEL:
                ttrLoad = new Texture("data/crate.png");
                newScreen = new GameScreen();
                break;
            case SETUP:
                ttrLoad = new Texture("data/redbote.png");
                newScreen = new SelectScreen();
                break;
        }
        ttrBackDrop = ttrLoad;
        bitmapFont.getData().setScale(GameWorld.FONT_X_SCALE, GameWorld.FONT_Y_SCALE);
        isLoaded = false;
    }

    private static final String STR_READY = "Ready!";
    private static final String STR_PRESENTING = "Presenting ... ";
    private static final String STR_LOADING = "Loading ... ";
    private String labelString = "invalid";

    @Override
    public void render(float delta) {

        final float barWidth = 20.0f;
        final float barLocX = GameWorld.VIRTUAL_WIDTH / 4.0f;
        final float barLocY = (GameWorld.VIRTUAL_HEIGHT / 2.0f) - 5;

        Gdx.gl.glViewport(0, 0, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
        Gdx.input.setInputProcessor(stage);

        if (screenTimer > 0) {
            screenTimer -= 1;
        }

        spriteBatch.begin();
        spriteBatch.draw(ttrBackDrop, 0, 0, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
        bitmapFont.draw(spriteBatch, labelString, barLocX, (GameWorld.VIRTUAL_HEIGHT / 5.0f) * 3);
        spriteBatch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (ScreenTypes.SETUP == screenType) {
            if (!isLoaded) {
                // show loading bar
                shapeRenderer.setColor(splashBarColor);
                shapeRenderer.rect(barLocX, barLocY, barWidth + loadCounter, 1);
            } else {
                // fade screen?
                if (screenTimer > 0 && screenTimer < 60) {
                    alpha -= 1.0f / 60;
                    fadeoutColor.a = alpha;
                    shapeRenderer.setColor(fadeoutColor);
                    shapeRenderer.rect(0, 0, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
                }
            }
        } else {
            shapeRenderer.setColor(loadBarColor);
            shapeRenderer.rect(barLocX, barLocY, barWidth + loadCounter, 10);
        }
        shapeRenderer.end();

        /*
         * make sure showScreen() not called until rendering pass ... hide() calls dispose()!
         */
        if (!isLoaded) {

            if (ScreenTypes.SETUP == screenType) {
                labelString = STR_PRESENTING;
            } else {
                labelString = STR_LOADING;
            }
            // make the bar up to half the screen width
            loadCounter = (int) (GameWorld.VIRTUAL_WIDTH * 0.5f * SceneLoader.getAssets().getProgress());

// todo if resource file missing, .... need exception handler here
            if (SceneLoader.getAssets().update()) {
                SceneLoader.doneLoading();
                isLoaded = true;
            }
        } else { // is loaded
            // one-time check for TS input (determines whether or not to show touchpad control)
            boolean isTouched = Gdx.input.isTouched(0);
            // set global status of touch screen for dynamic configuring of UI on-screen touchpad etc.
            // Once global "isTouchscreen" is set, don't clear it
            if (!GameWorld.getInstance().getIsTouchScreen() && isTouched) {
                GameWorld.getInstance().setIsTouchScreen(true);
            }

            if (ScreenTypes.LEVEL == screenType) {
                screenTimer = 0;
                labelString = STR_READY;

                if (stage.mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_A)
                        || Gdx.input.isTouched(0)) {
                    GameWorld.getInstance().showScreen(newScreen);
                }
            } else {
                // show setup screen immediately
                GameWorld.getInstance().showScreen(newScreen);
            }
        }
    }

    /*
     * required implementations of abstract methods
     */
    @Override
    public void pause() { // mt
    }

    @Override
    public void resume() { // mt
    }

    @Override
    public void resize(int width, int height) { // mt
    }

    @Override
    public void dispose() {
        ttrLoad.dispose();
        ttrSplash.dispose();
        spriteBatch.dispose();
        shapeRenderer.dispose();
        bitmapFont.dispose();
        stage.dispose();
    }

    @Override
    public void hide() {
        dispose();
    }
}
