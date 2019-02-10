/*
 * Copyright (c) 2019 Glenn Neidermeier
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
import com.mygdx.game.GameWorld;

/**
 * Created by neiderm on 12/18/17.
 */

   /*
    * Reference:
    *  on-screen menus:
    *   https://www.gamedevelopment.blog/full-libgdx-game-tutorial-menu-control/
    *  UI skin defined programmatically:
    *   https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/UISimpleTest.java
    */

public class MainMenuScreen implements Screen /* extends Stage */ {

    private InputMapper mapper = new InputMapper();
    private Stage stage; // I think we need to extend stage (like did for GamePad) in order to Override keyDown
    private ButtonGroup<TextButton> bg;
    private Texture buttonTexture;

    private Screen getLoadingScreen() {
        return new LoadingScreen("GameData.json");
    }


    MainMenuScreen() {
//super() ?
        stage = new Stage(new ScreenViewport())
/*        {
            @Override
            public boolean keyDown(int keycode) {

                if (keycode == Input.Keys.SPACE) {
//                    GameWorld.getInstance().showScreen(new GameScreen());  // Invalid, can't have gameScreen before loadScreen sceneLoader is initilaized!
                    GameWorld.getInstance().showScreen(getLoadingScreen());
                }

                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                    GameWorld.getInstance().showScreen(new SplashScreen());
                }
                return false;
            }
        }*/;

        Gdx.input.setInputProcessor(stage);
    }


    private final InputListener buttonBListener = new InputListener() {

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

//            GameWorld.getInstance().showScreen(new GameScreen());  // Invalid, can't have gameScreen before loadScreen sceneLoader is initilaized!
//            GameWorld.getInstance().showScreen(getLoadingScreen());
//            Gdx.input.setCatchBackKey(true);

            mapper.setInputState(InputMapper.InputState.INP_SELECT);

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

    private int previousIncrement;

    private int checkedUpDown(int step, int checkedIndex){

        final int N_SELECTIONS = 3;

        int selectedIndex = checkedIndex;

        if (0 == previousIncrement)
            selectedIndex += step;

        previousIncrement = step;

        if (selectedIndex >= N_SELECTIONS)
            selectedIndex = 0;
        if (selectedIndex < 0)
            selectedIndex = N_SELECTIONS - 1;

        return selectedIndex;
    }

    @Override
    public void render(float delta) {

//        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(66.0f / 255, 66.0f / 255, 231.0f / 255, 1.f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        stage.act();
        stage.draw();

        int idxCurSel = checkedUpDown(mapper.getDpad(null).getY(), bg.getCheckedIndex());
        setCheckedBox(idxCurSel);

        if (InputMapper.InputState.INP_SELECT == mapper.getInputState()) {

            switch (idxCurSel) {
                default:
                case 0:
                    GameWorld.getInstance().showScreen(getLoadingScreen());
                    break;
                case 1:
                    GameWorld.getInstance().showScreen( /* ScreenEnum screenEnum, Object... params */
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
          /*
    https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
    We need to update the stage's viewport in the resize method. The last Boolean argument set the origin to the lower left coordinate, causing the label to be drawn at that location.
     */
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {

        buttonTexture.dispose();
        stage.dispose();
        uiSkin.dispose();
    }

    // temporary until we have asset manager in
    private Skin uiSkin = new Skin(Gdx.files.internal("skin/uiskin.json"));

    @Override
    public void show() {

        // I put startup stuff in here simply because example I was following.

        Table table = new Table();
        table.setFillParent(true);
        table.setDebug(true);
        stage.addActor(table);


        //create buttons
        TextButton newGame = new TextButton("New Game", uiSkin, "toggle");
        TextButton preferences = new TextButton("Preferences", uiSkin, "toggle");
        TextButton exit = new TextButton("Exit", uiSkin, "toggle");


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
        table.add(newGame).fillX().uniformX();
        table.row().pad(10, 0, 10, 0);
        table.add(preferences).fillX().uniformX();
        table.row();
        table.add(exit).fillX().uniformX();

        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        button.setColor(1, 0, 0, 1);
        button.fillCircle(25, 25, 25);

        buttonTexture = new Texture(button);
        button.dispose();
        TextureRegion myTextureRegion = new TextureRegion(buttonTexture);
        TextureRegionDrawable myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);

        ImageButton buttonB = new ImageButton(myTexRegionDrawable);
        buttonB.setPosition(3 * Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 9f);
        buttonB.addListener(buttonBListener);
        table.row().padLeft(10);
        table.add(buttonB);
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
