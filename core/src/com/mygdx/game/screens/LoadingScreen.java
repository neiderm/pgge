package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.game.SceneLoader;

/**
 * Created by utf1247 on 7/16/2018.
 * based on:
 * http://www.pixnbgames.com/blog/libgdx/how-to-make-a-splash-screen-in-libgdx/?_sm_nck=1
 */

public class LoadingScreen implements Screen {

    private ShapeRenderer shapeRenderer = new ShapeRenderer();

    private SpriteBatch batch;
    private Texture ttrSplash;

    private int loadCounter = 0;
    private boolean isLoaded;


    public LoadingScreen() {

        batch = new SpriteBatch();
        ttrSplash = new Texture("data/crate.png");

        font = new BitmapFont(
                Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);
        font.getData().setScale(1.0f);

        isLoaded = false;

        // hmm yep hackish awright
        if (null != GameWorld.sceneLoader){
            GameWorld.sceneLoader.dispose();
            GameWorld.sceneLoader = null;
        }
//        sceneLoader = new SceneLoader();
        GameWorld.sceneLoader = new SceneLoader();  // bah

        // not using a listener for now, we just need to make sure we haven't left a stale "unattended" input processor lying around!
        Gdx.input.setInputProcessor(new Stage());
    }


    private StringBuilder stringBuilder = new StringBuilder();
    private BitmapFont font;

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(255, 255, 255, 1));
        shapeRenderer.rect(
                (Gdx.graphics.getWidth() / 2f) - 5, (Gdx.graphics.getHeight() / 2f) - 5,
                20f + loadCounter, 10);
        shapeRenderer.end();

        if (!isLoaded) {

            stringBuilder.setLength(0);
            stringBuilder.append("Loading ... ");

            loadCounter += 1;

            if (GameWorld.sceneLoader.getAssets().update()) {
                GameWorld.sceneLoader.doneLoading();
                isLoaded = true;
            }
        } else {

            stringBuilder.setLength(0);
            stringBuilder.append("Ready!");

            // simple polling for a tap on the touch screen
            if (Gdx.input.isTouched(0)
                    || (Gdx.input.isKeyPressed(Input.Keys.SPACE))
                    ) {
                GameWorld.getInstance().showScreen(new GameScreen());
//                Gdx.input.setCatchBackKey(true);
            }
            // there is no ESCape from Loading screen, but maybe we would. Have to be careful about disposing scene loader.
/*if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyPressed(Input.Keys.BACK)){
    GameWorld.getInstance().showScreen(new MainMenuScreen());
}*/
        }

        batch.begin();
        batch.draw(ttrSplash, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        font.draw(batch, stringBuilder,
                Gdx.graphics.getWidth() / 4f, (Gdx.graphics.getHeight() / 4f) * 3f);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(255, 255, 255, 1));
        shapeRenderer.rect(
                (Gdx.graphics.getWidth() / 2f) - 5, (Gdx.graphics.getHeight() / 2f) - 5,
                20f + loadCounter, 10);
        shapeRenderer.end();
    }

    @Override
    public void hide() {         // mt
//dispose();// hhhhhhmmmmmm
        ttrSplash.dispose();
//shapeRenderer.dispose();
        //        batch.dispose();
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
