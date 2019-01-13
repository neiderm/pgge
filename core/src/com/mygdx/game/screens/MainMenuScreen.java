package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.characters.InputStruct;

/**
 * Created by mango on 12/18/17.
 */

// https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/UISimpleTest.java#L37
// make sure this not visible outside of com.mygdx.game.screens
public class MainMenuScreen implements Screen /* extends Stage */ {

    private InputStruct mapper = new InputStruct();
    private Stage stage; // I think we need to extend stage (like did for GamePad) in order to Override keyDown
    private Controller connectedCtrl;
    private ButtonGroup<TextButton> bg;

    private Screen getLoadingScreen() {
        return new LoadingScreen("GameData.json");
    }


    public MainMenuScreen() {

        int width = Gdx.graphics.getWidth() / 2;
        int height = Gdx.graphics.getHeight() / 2;

//        Gdx.graphics.setWindowedMode(800,600);

        //https://github.com/dfour/box2dtut/blob/master/box2dtut/core/src/blog/gamedevelopment/box2dtutorial/views/EndScreen.java
        // create stage and set it as input processor
        stage = new Stage(new ScreenViewport())
/*        {

            @Override
            public boolean keyDown(int keycode) {

                if (keycode == Input.Keys.SPACE) {
//                    GameWorld.getInstance().showScreen(new GameScreen());  // Invalid, can't have gameScreen before loadScreen sceneLoader is initilaized!
                    GameWorld.getInstance().showScreen(getLoadingScreen());
//                    Gdx.input.setCatchBackKey(true);
                }

                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                    // I don't know if it's not getting debounced coming off of SelectScreen?
                    Gdx.app.log("MainMenuScreen", "keyDown = " + keycode);
                    GameWorld.getInstance().showScreen(new SplashScreen());
                }
                return false;
            }
        }*/
        ;

        // If a controller is connected, find it and grab a link to it
        connectedCtrl = InputStruct.getConnectedCtrl(0);

//        Gdx.input.setCatchBackKey(true);
        Gdx.input.setInputProcessor(stage);
    }


    private final InputListener buttonBListener = new InputListener() {

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

//            GameWorld.getInstance().showScreen(new GameScreen());  // Invalid, can't have gameScreen before loadScreen sceneLoader is initilaized!
//            GameWorld.getInstance().showScreen(getLoadingScreen());
//            Gdx.input.setCatchBackKey(true);

            mapper.setKeyDown(InputStruct.INP_SELECT);

            return false;
        }
    };

    private int checkedBox;

    private void setCheckedBox(int checked) {
        switch (checked) {
            default:
            case 0:
                bg.setChecked("New Game");
                break;
            case 1:
                bg.setChecked("Preferences");
                break;
            case 2:
                bg.setChecked("Exit");
                break;
        }
    }

    private final int numCkBoxes = 3;
    private int previousIncrement;

    boolean asdf = false;

    @Override
    public void render(float delta) {

        if (asdf)
            Gdx.app.log("MainMenuScreen", "asd;lfkjasd;lfjasd;lfsf;lkdsjfdsl;fjslksjl;fajladskjflsfj");

        int checked = bg.getCheckedIndex();

        int dPadXaxis = 0;
        PovDirection povDir = null;

// TODO: get up/down keyboard here
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            dPadXaxis = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            dPadXaxis = 1;
        }

        if (null != connectedCtrl) {
            povDir = connectedCtrl.getPov(0); // povCode ...
        }
            if (PovDirection.north == povDir) {
                dPadXaxis = -1;
            } else if (PovDirection.south == povDir) {
                dPadXaxis = +1;
            }


        if (0 == previousIncrement)
            checked += dPadXaxis;

        previousIncrement = dPadXaxis;

        if (checked >= numCkBoxes)
            checked = 0;
        if (checked < 0)
            checked = numCkBoxes - 1;


        setCheckedBox(checked);

//        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(66.0f / 255, 66.0f / 255, 231.0f / 255, 1.f);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        stage.act();
        stage.draw();

        boolean selectBtnPressed = false;
        if (null != connectedCtrl) {
            selectBtnPressed = connectedCtrl.getButton(Input.Buttons.LEFT); //  // "X Button "
        }
        int inputState = mapper.getKeyDown();

        if (selectBtnPressed || InputStruct.INP_SELECT == inputState /* || Gdx.input.isTouched(0) */) {

            asdf = true;

            switch (checked) {
                default:
                case 0:
                    GameWorld.getInstance().showScreen(getLoadingScreen());
                    break;
                case 1:
                    GameWorld.getInstance().showScreen(
                            new LoadingScreen(dataFileName, false, LoadingScreen.ScreenTypes.SETUP));
                    break;
                case 2:
                    GameWorld.getInstance().showScreen(new SplashScreen());
                    break;
            }
        }
    }


    private static String dataFileName = "SelectScreen.json";


    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {

        stage.dispose();
    }

    @Override
    public void show() {
//        Gdx.input.setCatchBackKey(false);   // TODO: is there any place we would want this?

        // create table to layout items we will add
        Table table = new Table();
        table.setFillParent(true);
        table.setDebug(true);
        stage.addActor(table);

        // temporary until we have asset manager in
        Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

        //create buttons
        TextButton newGame = new TextButton("New Game", skin, "toggle");
        TextButton preferences = new TextButton("Preferences", skin, "toggle");
        TextButton exit = new TextButton("Exit", skin, "toggle");


        bg = new ButtonGroup<TextButton>(newGame, preferences, exit);
        bg.setMaxCheckCount(1);
        bg.setMinCheckCount(1);
        setCheckedBox(checkedBox);

        // create button listeners
        exit.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
//                Gdx.app.exit();
//                GameWorld.getInstance().showScreen(new SplashScreen());
//                setCheckedBox(0);
                checkedBox = 0;
            }
        });

        newGame.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                //             GameWorld.getInstance().showScreen(getLoadingScreen());
//                GameWorld.getInstance().showScreen(new LoadingScreen(dataFileName, false, LoadingScreen.ScreenTypes.SETUP));
//                setCheckedBox(2);
                checkedBox = 2;
            }
        });

        //add buttons to table
///*
        table.add(newGame).fillX().uniformX();
        table.row().pad(10, 0, 10, 0);
        table.add(preferences).fillX().uniformX();
        table.row();
        table.add(exit).fillX().uniformX();
//*/

        ///*
        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        button.setColor(1, 0, 0, 1);
        button.fillCircle(25, 25, 25);
        Texture myTexture = new Texture(button);
        button.dispose();
        TextureRegion myTextureRegion = new TextureRegion(myTexture);
        TextureRegionDrawable myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);

        ImageButton buttonB = new ImageButton(myTexRegionDrawable);
        buttonB.setPosition(3 * Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 9f);
        buttonB.addListener(buttonBListener);
        table.row().padLeft(10);
        table.add(buttonB);
//*/
    }

    @Override
    public void pause() {
        // empty
    }

    @Override
    public void resume() {
        // empty
    }

    @Override
    public void hide() {

        dispose();
    }
}
