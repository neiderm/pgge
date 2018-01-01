package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.MyGdxGame;

/**
 * Created by mango on 12/18/17.
 */

public class MainMenuScreen implements Screen {

    MyGdxGame game;

    //    Sprite box;
    BitmapFont font;
    OrthographicCamera guiCam;
    SpriteBatch batch;
    ShapeRenderer shapeRenderer;

    private static int touchBoxW = 20, touchBoxH = 10;

    private class MyInputAdapter extends InputAdapter {
        @Override
        public boolean touchDown(int x, int y, int pointer, int button) {
            // your touch down code here
            return true; // return true to indicate the event was handled
        }

        @Override
        public boolean touchUp(int x, int y, int pointer, int button) {
            // your touch up code here

            game.setScreen(new GameScreen(game));

            return true; // return true to indicate the event was handled
        }
    }

    public MainMenuScreen(MyGdxGame game) {

        Gdx.graphics.setWindowedMode(800,600);
        this.game = game;

        font = new BitmapFont();
        guiCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        guiCam.position.set(guiCam.viewportWidth / 2f, guiCam.viewportHeight / 2f, 0);
        guiCam.update();
        batch = new SpriteBatch();
        //      box = new Sprite(new Texture("cube.png"));
        //      box = new Sprite();
        //      box.setPosition(0, 0);
        shapeRenderer = new ShapeRenderer();


        Gdx.input.setInputProcessor(new MyInputAdapter());
    }


    @Override
    public void render(float delta) {

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        //         Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);


        // GUI viewport (full screen)
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.setProjectionMatrix(guiCam.combined);
        batch.begin();
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0, Gdx.graphics.getHeight());
//        box.draw(batch);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.rect(
                (float)Gdx.graphics.getWidth() / 2 - touchBoxW / 2,
                (float)Gdx.graphics.getHeight() / 2 - touchBoxH / 2,
                touchBoxW, touchBoxH);
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void dispose() {
        font.dispose();
        batch.dispose();
        shapeRenderer.dispose();
    }

    @Override
    public void show() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }
}
