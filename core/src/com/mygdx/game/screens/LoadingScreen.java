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

    private int loadCounter = 0;
    private int screenTimer = (int) (60 * 2.9f); //fps*sec
    private boolean isLoaded;
    private boolean shouldPause;
    private ScreenTypes screenType;
    private InputMapper mapper;
    private Screen newScreen;
    private Texture ttrBackDrop; // reference to selected texture (does not need disposed)

    // disposables
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Texture ttrSplash;
    private Texture ttrLoad;
    private SpriteBatch spriteBatch;
    private BitmapFont bitmapFont;

    public enum ScreenTypes {
        SETUP,
        LEVEL
    }

    public LoadingScreen(boolean shouldPause, ScreenTypes screenType) {
        this.shouldPause = shouldPause;
        this.screenType = screenType;
    }

    LoadingScreen() {
        this(true, LEVEL);
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
        spriteBatch = new SpriteBatch();
        bitmapFont = new BitmapFont(
                Gdx.files.internal(GameWorld.DEFAULT_FONT_FNT),
                Gdx.files.internal(GameWorld.DEFAULT_FONT_PNG), false);
        bitmapFont.getData().setScale(1.0f);
        isLoaded = false;
        mapper = new InputMapper();
    }

    /*
     * re-use the String Builder instance
     */
    private final StringBuilder stringBuilder = new StringBuilder();
    /*
     * alpha persistent for fadeout
     */
    private float alpha = 1.0f;

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriteBatch.begin();
        spriteBatch.draw(ttrBackDrop, 0, 0, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
        bitmapFont.draw(spriteBatch, stringBuilder,
                GameWorld.VIRTUAL_WIDTH / 4.0f, (GameWorld.VIRTUAL_HEIGHT / 5.0f) * 3);
        spriteBatch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (ScreenTypes.SETUP == screenType) {
            if (!isLoaded) {
                // show loading bar
                shapeRenderer.setColor(new Color(Color.BLACK)); // only while loading
                shapeRenderer.rect(
                        (GameWorld.VIRTUAL_WIDTH / 4.0f), (GameWorld.VIRTUAL_HEIGHT / 2.0f) - 5,
                        20.0f + loadCounter, 1);
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
            // one-time check for TS input (determines whether or not to show touchpad control)
            boolean isTouched = Gdx.input.isTouched(0);

            // set global status of touch screen for dynamic configuring of UI on-screen touchpad etc.
            // (but once global "isTouchscreen" is set, don't clear it ;)
            if (!GameWorld.getInstance().getIsTouchScreen() && isTouched) {
                GameWorld.getInstance().setIsTouchScreen(true);
            }

            if (0 == screenTimer || !shouldPause) {
                final float AxisThreshold = 0.8f;

                InputMapper.InputState inp = mapper.getInputState();

                if ((InputMapper.InputState.INP_FIRE1 == inp) || isTouched) {
                    GameWorld.getInstance().showScreen(newScreen);

                } else if ((ScreenTypes.SETUP == screenType) &&
                        (InputMapper.InputState.INP_MENU == inp ||
                                mapper.getAxis(InputMapper.VIRTUAL_AD_AXIS) > AxisThreshold)) {

                    GameWorld.getInstance().showScreen(new GamepadConfig());
                }
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
    }

    @Override
    public void hide() {
        dispose();
    }
}
