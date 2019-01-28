package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.characters.InputStruct;

/**
 * Created by neiderm on 7/16/2018.
 * based on:
 * http://www.pixnbgames.com/blog/libgdx/how-to-make-a-splash-screen-in-libgdx/?_sm_nck=1
 */

public class SplashScreen implements Screen {

    private static String dataFileName = "SelectScreen.json";
    private SpriteBatch batch;
    private Texture ttrSplash;
    private InputStruct mapper;
    private InputStruct.InputState preInputState;

    SplashScreen() {
        batch = new SpriteBatch();
        ttrSplash = new Texture("splash-screen.png");
        mapper = new InputStruct();
        preInputState =  mapper.getInputState(); // initial value for debouncing stupid Select input (from game-pad)
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

        InputStruct.InputState inputState =  mapper.getInputState();

        if (InputStruct.InputState.INP_SELECT == inputState
                && InputStruct.InputState.INP_SELECT != preInputState) {

            GameWorld.getInstance().showScreen(new LoadingScreen(dataFileName, false, LoadingScreen.ScreenTypes.SETUP));
            Gdx.app.log("Splash Screen", "-> GameWorld.getInstance().showScreen(new LoadingScreen");
        }
        preInputState = inputState;
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
    public void show() {  // mt
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
