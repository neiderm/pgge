package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * Created by utf1247 on 7/16/2018.
 * based on:
 * http://www.pixnbgames.com/blog/libgdx/how-to-make-a-splash-screen-in-libgdx/?_sm_nck=1
 */

public class SplashScreen implements Screen {

    private SpriteBatch batch;
    private Texture ttrSplash;
    private Texture spinner;

    private TextureRegion region;
    private float rotation;
//    private boolean isLoaded;


    SplashScreen() {
        batch = new SpriteBatch();
        ttrSplash = new Texture("splash-screen.png");
        spinner = new Texture("ship_icon.png");
        region = new TextureRegion(spinner);
//        isLoaded = false;

// TODO: maybe not need to go thru Stage, but set a minimal InputProcessor for keyDown()   .... hit the any ket to begin!
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(ttrSplash, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        float width = region.getRegionHeight();
        float height = region.getRegionWidth();

        if (false /*!isLoaded*/) {
            batch.draw(region,
                    Gdx.graphics.getWidth() / 2.0f - width / 2.0f,
                    Gdx.graphics.getHeight() / 2.0f - height / 2.0f,
                    width / 2, height / 2, // originX, originY,
                    width, height,
                    1, 1, // scaleX, scaleY,
                    rotation += 6
            );
        }

        batch.end();

//        if (!isLoaded)
        {
/*
            if (GameWorld.assets.update()) {
                GameWorld.sceneLoader.doneLoading();
                isLoaded = true;
            }
*/
        }
//        else
        {

            // set global status of touch screen for dynamic configuring of UI on-screen touchpad etc.
            // (but once global "isTouchscreen" is set, don't clear it ;)
            if (!GameWorld.getInstance().getIsTouchScreen()) {
//            GameWorld.getInstance().setIsTouchScreen(Gdx.input.isTouched(0));
                boolean isTouched = Gdx.input.isTouched(0);
                if (isTouched)
                    GameWorld.getInstance().setIsTouchScreen(isTouched);
            }

            // simple polling for a tap on the touch screen
            if (GameWorld.getInstance().getIsTouchScreen()
                    || Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
                GameWorld.getInstance().showScreen(new LoadingScreen());
                Gdx.input.setCatchBackKey(true);
            }
        }

        // not using a listener right now ... make sure we haven't left a stale "unattended" input processor lying around!
        Gdx.input.setInputProcessor(new Stage());
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void dispose() {
        ttrSplash.dispose();
        spinner.dispose();
        batch.dispose();
    }
}
