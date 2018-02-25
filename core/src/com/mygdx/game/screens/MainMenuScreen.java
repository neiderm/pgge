package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.GamePad;
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
    GamePad stage;

    private static int touchBoxW = 20, touchBoxH = 10;


    public MainMenuScreen(MyGdxGame game) {

//        Gdx.graphics.setWindowedMode(800,600);
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

        stage = new GamePad(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeListener.ChangeEvent event, Actor actor) {}
                },
                new InputListener() {},
                buttonBListener);

        Gdx.input.setInputProcessor(stage);
    }


    private final InputListener buttonBListener = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            return true;
        }
        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            game.setScreen(new GameScreen(game));
        }
    };



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
                (float)Gdx.graphics.getWidth() / 2 - touchBoxW / 2f,
                (float)Gdx.graphics.getHeight() / 2 - touchBoxH / 2f,
                touchBoxW, touchBoxH);
        shapeRenderer.end();


        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
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
