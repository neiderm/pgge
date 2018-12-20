package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * Created by utf1247 on 7/16/2018.
 * based on:
 * http://www.pixnbgames.com/blog/libgdx/how-to-make-a-splash-screen-in-libgdx/?_sm_nck=1
 */

public class SplashScreen implements Screen {

    private SpriteBatch batch;
    private Texture ttrSplash;


    SplashScreen() {
        batch = new SpriteBatch();
        ttrSplash = new Texture("splash-screen.png");

        // not using a listener right now ... make sure we haven't left a stale "unattended" input processor lying around!
        Gdx.input.setInputProcessor(new Stage());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(ttrSplash, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        boolean isTouched = Gdx.input.isTouched(0);

        // set global status of touch screen for dynamic configuring of UI on-screen touchpad etc.
        // (but once global "isTouchscreen" is set, don't clear it ;)
        if (!GameWorld.getInstance().getIsTouchScreen() && isTouched)
                GameWorld.getInstance().setIsTouchScreen(true);

        // simple polling for a tap on the touch screen
        if (isTouched
                || Gdx.input.isKeyPressed(Input.Keys.SPACE)) {

            GameWorld.getInstance().showScreen(new LoadingScreen());
//            Gdx.input.setCatchBackKey(true);

            Gdx.app.log("Splash Screen", "-> GameWorld.getInstance().showScreen(new LoadingScreen");
        }
    }

    @Override
    public void hide() {  // mt
//TODO:?        dispose(); // tear down
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
