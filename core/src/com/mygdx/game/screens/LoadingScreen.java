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

import static com.mygdx.game.screens.LoadingScreen.ScreenTypes.LEVEL;

/**
 * Created by neiderm on 7/16/2018.
 * based on:
 * http://www.pixnbgames.com/blog/libgdx/how-to-make-a-splash-screen-in-libgdx/?_sm_nck=1
 */

public class LoadingScreen implements Screen {

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    private SpriteBatch batch;
    private Texture ttrSplash;
    private Texture ttrLoad;
    private Texture ttrBackDrop;
    private int loadCounter = 0;
    private int screenTimer = (int) (60 * 2.9f); //fps*sec
    private boolean isLoaded;
    private boolean shouldPause = true;
    private ScreenTypes screenType = LEVEL;
    private InputMapper mapper;
    private Screen newScreen;

    public enum ScreenTypes {
        SETUP,
        LEVEL
    }

    LoadingScreen(boolean shouldPause, ScreenTypes screenType) {
        this.shouldPause = shouldPause;
        this.screenType = screenType;
    }

    LoadingScreen() {    //mt
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
        ttrSplash = new Texture("splash-screen.png");

        batch = new SpriteBatch();

        font = new BitmapFont(
                Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);
        font.getData().setScale(1.0f);

        isLoaded = false;

        mapper = new InputMapper();
    }

    private final StringBuilder stringBuilder = new StringBuilder();
    private BitmapFont font;
    private float alpha = 1.0f;

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        batch.draw(ttrBackDrop, 0, 0, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);

        font.draw(batch, stringBuilder,
                GameWorld.VIRTUAL_WIDTH / 4f, (GameWorld.VIRTUAL_HEIGHT / 5f) * 3f);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

// if screentype == whatever?
        if (ScreenTypes.SETUP == screenType) {

            if (!isLoaded) {
                // show loading bar
                shapeRenderer.setColor(new Color(Color.BLACK)); // only while loading
                shapeRenderer.rect(
                        (GameWorld.VIRTUAL_WIDTH / 4f), (GameWorld.VIRTUAL_HEIGHT / 2f) - 5,
                        20f + loadCounter, 1);
            } else {
                // fade screen?
                    if (screenTimer > 0 && screenTimer < 60) {
                        alpha -= 1.0f / 60;
                        shapeRenderer.setColor(new Color(0, 0, 0, 1 - alpha));
                        shapeRenderer.rect(0, 0,
                                GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
                    }
            }
        } else {
            shapeRenderer.setColor(new Color(255, 0, 0, 1));
            shapeRenderer.rect(
                    (GameWorld.VIRTUAL_WIDTH / 4f), (GameWorld.VIRTUAL_HEIGHT / 2f) - 5,
                    20f + loadCounter, 10);
        }
        shapeRenderer.end();

        if (screenTimer > 0) {
            screenTimer -= 1;
        }

        /*
         * make sure loadNewScreen() not called until rendering pass ... hide() destroys everything!
         */
        if (!isLoaded) {

            if (ScreenTypes.SETUP == screenType) {
                stringBuilder.setLength(0);
                stringBuilder.append("Presenting ... ");
            } else {
                stringBuilder.setLength(0);
                stringBuilder.append("Loading ... ");
            }

            // make the bar up to half the screen width
            loadCounter =
                    (int) (GameWorld.VIRTUAL_WIDTH * 0.5f * SceneLoader.getAssets().getProgress());

            if (SceneLoader.getAssets().update()) {
                SceneLoader.doneLoading();
                isLoaded = true;
            }
        } else { // is loaded

            if (ScreenTypes.SETUP == screenType) {

                if (screenTimer > 0) {
                    // mt
                } else {
                    ttrBackDrop = ttrSplash;
                    stringBuilder.setLength(0);
                    stringBuilder.append("Tap to Start!");
                }
            } else {
                screenTimer = 0;
                stringBuilder.setLength(0);
                stringBuilder.append("Ready!");
            }

            // simple polling for a tap on the touch screen
            // one-time check for TS input (determine wether to show touchpad controll)
            boolean isTouched = Gdx.input.isTouched(0);

            // set global status of touch screen for dynamic configuring of UI on-screen touchpad etc.
            // (but once global "isTouchscreen" is set, don't clear it ;)
            if (!GameWorld.getInstance().getIsTouchScreen() && isTouched) {
                GameWorld.getInstance().setIsTouchScreen(true);
            }

            InputMapper.InputState inp = mapper.getInputState(true);

            if (0 == screenTimer || !shouldPause) {

                final float AxisThreshold = 0.8f;
                if (  InputMapper.InputState.INP_FIRE1 == inp ) {
                    GameWorld.getInstance().showScreen(newScreen);
                }
                else if ((ScreenTypes.SETUP == screenType) &&
                        (InputMapper.InputState.INP_MENU == inp ||
                                mapper.getAxisX(0) > AxisThreshold)) {
                        GameWorld.getInstance().showScreen(new GamepadConfig());
                }
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
    public void resize(int width, int height) {        // mt
    }

    @Override
    public void dispose() {
        ttrLoad.dispose();
        ttrSplash.dispose();
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
