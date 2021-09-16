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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.GameWorld;
import com.mygdx.game.sceneLoader.SceneLoader;

import java.util.Locale;

/**
 * Created by neiderm on 5/17/2018.
 */
public class GameUI extends InGameMenu {

    private static final int KEY_CODE_POV_UP = Input.Keys.DPAD_UP;
    private static final int KEY_CODE_POV_DOWN = Input.Keys.DPAD_DOWN;
    private static final int KEY_CODE_POV_LEFT = Input.Keys.DPAD_LEFT;
    private static final int KEY_CODE_POV_RIGHT = Input.Keys.DPAD_RIGHT;
    private static final int DEFAULT_SCREEN_TIME = 60 * 60; // FPS
    private static final int TIME_LIMIT_WARN_SECS = 10;

    static final int SCREEN_CONTINUE_TIME = 10 * 60; // FPS
    private final Color hudOverlayColor;
    private final StringBuilder stringBuilder = new StringBuilder();

    private ImageButton picButton;
    private ImageButton xButton;
    private Touchpad touchpad;
    private int msgLabelCounter;
    private int score; // my certain game the score resets every screen anyway so who cares
    private int screenTimer = DEFAULT_SCREEN_TIME;

    // @dispoable
    private Skin touchpadSkin;
    private Texture tpBackgnd;
    private Texture tpKnob;

    // too bad so sad package-private vars are messed w/ by gamescreeen
    boolean canExit; // exit sensor is tripped
    int prizeCount;
    int continueScreenTimeUp;

    GameUI() {
        //this.getViewport().getCamera().update(); // GN: hmmm I can get the camera
//        super(null, "Paused");
        super(DEFAULT_UISKIN_JSON, "Paused");

        // start with White, alpha==0 and fade to Black with alpha=1
        hudOverlayColor = new Color(1, 1, 1, 0);

        // hack ...assert default state for game-screen unpaused since use it as a visibility flag for on-screen menu!
        GameWorld.getInstance().setIsPaused(false);

        setupOnscreenControls();
        setupInGameMenu();

        addTouchPad(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Touchpad t = (Touchpad) actor;
                mapper.setAxis(0, t.getKnobPercentX());
                mapper.setAxis(1, t.getKnobPercentY() * (-1));
            }
        });
        // anything else a sub-class needs to do can be overridden
        init();
    }

    /*
     * so it can be overridden
     */
    protected void init() { // mt
    }

    int getScreenTimer() {
        return screenTimer;
    }

    @Override
    public boolean keyDown(int keycode) {

        int axisSetIndexX = InputMapper.VIRTUAL_AD_AXIS;
        int axisSetIndexY = InputMapper.VIRTUAL_WS_AXIS;

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            axisSetIndexX = InputMapper.VIRTUAL_X1_AXIS; // right anlg stick "X" (if used)
            axisSetIndexY = InputMapper.VIRTUAL_Y1_AXIS; // right anlg stick "Y" (if used)
        }
        if (KEY_CODE_POV_LEFT == keycode) {
            mapper.setAxis(axisSetIndexX, -1);
        }
        if (KEY_CODE_POV_RIGHT == keycode) {
            mapper.setAxis(axisSetIndexX, +1);
        }
        if (KEY_CODE_POV_UP == keycode) {
            mapper.setAxis(axisSetIndexY, -1);
        }
        if (KEY_CODE_POV_DOWN == keycode) {
            mapper.setAxis(axisSetIndexY, +1);
        }
        if (Input.Keys.SPACE == keycode) {
            mapper.setControlButton(BTN_KCODE_FIRE1, true);
        }
        if (Input.Keys.CONTROL_LEFT == keycode) {
            mapper.setControlButton(BTN_KCODE_FIRE2, true);
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {

        int axisSetIndexX = InputMapper.VIRTUAL_AD_AXIS;
        int axisSetIndexY = InputMapper.VIRTUAL_WS_AXIS;

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            axisSetIndexX = InputMapper.VIRTUAL_X1_AXIS; // right anlg stick "X" (if used)
            axisSetIndexY = InputMapper.VIRTUAL_Y1_AXIS; // right anlg stick "Y" (if used)
        }

        if (KEY_CODE_POV_LEFT == keycode && !Gdx.input.isKeyPressed(KEY_CODE_POV_RIGHT) ||
                KEY_CODE_POV_RIGHT == keycode && !Gdx.input.isKeyPressed(KEY_CODE_POV_LEFT)) {
            mapper.setAxis(axisSetIndexX, 0);
        }
        if (KEY_CODE_POV_UP == keycode && !Gdx.input.isKeyPressed(KEY_CODE_POV_DOWN) ||
                KEY_CODE_POV_DOWN == keycode && !Gdx.input.isKeyPressed(KEY_CODE_POV_UP)) {
            mapper.setAxis(axisSetIndexY, 0);
        }
        if (Input.Keys.SPACE == keycode) {
            mapper.setControlButton(BTN_KCODE_FIRE1, false);
        }
        if (Input.Keys.CONTROL_LEFT == keycode) {
            mapper.setControlButton(BTN_KCODE_FIRE2, false);
        }
        return false;
    }

    /**
     * Based on "http://www.bigerstaff.com/libgdx-touchpad-example" (link broken)
     */
    private void addTouchPad(ChangeListener touchPadChangeListener) {

        Touchpad.TouchpadStyle touchpadStyle;
        int tpRadius = 100;
        int knobRadius = 36;
        float scale = Gdx.graphics.getDensity();

        // fudge scaling to make UI controls visible on my HTC One M8
        if (scale > 1) {
            tpRadius = (int) (tpRadius * scale / 2f);
            knobRadius = (int) (knobRadius * scale / 2);
        }

        //Create a touchpad skin
        touchpadSkin = new Skin();

        //Set background image
//        touchpadSkin.add("touchBackground", new Texture("data/touchBackground.png"));
//        Pixmap.setBlending(Pixmap.Blending.None);
        //Set knob image
//        tpKnob = new Texture("data/touchKnob.png");
//        touchpadSkin.add("touchKnob", tpKnob);
        Pixmap button = new Pixmap(knobRadius * 2, knobRadius * 2, Pixmap.Format.RGBA8888);
        button.setColor(1, 0, 0, 0.5f);
        button.fillCircle(knobRadius, knobRadius, knobRadius);
        tpKnob = new Texture(button);

        touchpadStyle = new Touchpad.TouchpadStyle();
        //Create Drawable's from TouchPad skin
//        Drawable touchBackground = touchpadSkin.getDrawable("touchBackground");

// https://stackoverflow.com/questions/27757944/libgdx-drawing-semi-transparent-circle-on-pixmap
//        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap background = new Pixmap(tpRadius * 2, tpRadius * 2, Pixmap.Format.RGBA8888);
        background.setColor(1, 1, 1, .2f);
        background.fillCircle(tpRadius, tpRadius, tpRadius);

        tpBackgnd = new Texture(background);
        touchpadStyle.background = new TextureRegionDrawable(new TextureRegion(tpBackgnd));
//        touchpadStyle.knob = touchpadSkin.getDrawable("touchKnob");
        touchpadStyle.knob = new TextureRegionDrawable(new TextureRegion(tpKnob));

        touchpad = new Touchpad(10, touchpadStyle);
        //setBounds(x,y,width,height)
        touchpad.setBounds(15, 15, tpRadius * 2f, tpRadius * 2f);

        // RE touchpad.addListener
        //   https://gamedev.stackexchange.com/questions/127733/libgdx-how-to-handle-touchpad-input/127937#127937
        touchpad.addListener(touchPadChangeListener);
        this.addActor(touchpad);

        button.dispose();
        background.dispose();
    }

    private void setupInGameMenu() {
        addButton("Resume");
        addButton("Restart");
        addButton("Quit");
        addButton("Camera");
        addButton("Debug Draw");
        addNextButton();

        onscreenMenuTbl.setVisible(false); // default not visible (Paused menu)
    }

    private void setupOnscreenControls() {

        final int gsBTNwidth = Gdx.graphics.getHeight() * 3 / 8;
        final int gsBTNheight = Gdx.graphics.getHeight() * 3 / 8;
        // placement relative to absolute center of screen ... i guess
        final int gsBTNx = Gdx.graphics.getWidth() / 2 - gsBTNwidth / 2;
        final int gsBTNy = 0;

        picButton = addImageButton(
                gsBTNx + 0f, gsBTNy - 0f,
                gsBTNwidth, gsBTNheight,
                InputMapper.InputState.INP_FIRE1);

        xButton = addImageButton(
                3f * Gdx.graphics.getWidth() / 4, 0,
                Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 4,
                InputMapper.InputState.INP_FIRE2);
    }

    /*
     * make simple outlined button to provide either InputState or touch/pointer input
     */
    private ImageButton addImageButton(
            float btnX, float btnY, int btnWidth, int btnHeight, final InputMapper.InputState ips
    ) {
//        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap pixmap = new Pixmap(btnWidth, btnHeight, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, .3f);
        pixmap.drawRectangle(0, 0, btnWidth, btnHeight);
        Texture useTexture = new Texture(pixmap);
        ImageButton button = addImageButton(useTexture, btnX, btnY, ips);

        pixmap.dispose();
        return button;
    }

    private void updateTimerLbl() {

        int minutes = 0;
        int seconds = 0;

        if (!GameWorld.getInstance().getIsPaused()) {
            screenTimer -= 1;
        }

        int screenTimerSecs = screenTimer / 60; // FPS

        if (screenTimerSecs > 0) {
            minutes = screenTimerSecs / 60;
            seconds = screenTimerSecs % 60;
        }
        stringBuilder.setLength(0);
        stringBuilder.append(
                String.format(
                        Locale.ENGLISH, "%02d", minutes)).append(":").append(String.format(Locale.ENGLISH, "%02d", seconds));

        if (screenTimerSecs <= TIME_LIMIT_WARN_SECS) {
            setLabelColor(timerLabel, Color.RED);
        }

        timerLabel.setText(stringBuilder);
    }

    private void fadeScreen() {

        float step = -0.05f;
        float alphaStep = -step;

        if (hudOverlayColor.r > 0.1f)
            hudOverlayColor.r += step;

        if (hudOverlayColor.g > 0.1f)
            hudOverlayColor.g += step;

        if (hudOverlayColor.b > 0.1f)
            hudOverlayColor.b += step;

        if (hudOverlayColor.a < 1)
            hudOverlayColor.a += alphaStep;

        setOverlayColor(hudOverlayColor.r, hudOverlayColor.g, hudOverlayColor.b, hudOverlayColor.a);
    }

    void addScore(int points) {
        score += points;
    }

    void setScore(int points) {
        score = points;
    }

    private int getScore() {
        return score;
    }

    private int getPrizeCount() {
        return prizeCount;
    }

    public void onCameraSwitch() { // mt
    }

    @Override
    protected void onPauseEvent() {

        if (GameWorld.getInstance().getIsPaused()) {
            GameWorld.getInstance().setIsPaused(false);

            switch (getCheckedIndex()) {
                default:
                case 0: // resume
                    break;
                case 1: // restart
                    GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_RESTART);
                    break;
                case 2: // quit
                    GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_QUIT);
                    break;
                case 3: // camera
                    onCameraSwitch();  // to put in GameUI interface and override
                    break;
                case 4: // debug draw
                    BulletWorld.USE_DDBUG_DRAW = !BulletWorld.USE_DDBUG_DRAW;
                    // has to reinitialize bullet world to set the flag
                    GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_RESTART);
                    break;
            }
        }
    }

    @Override
    protected void onEscEvent() {

        if (GameWorld.GAME_STATE_T.ROUND_ACTIVE == GameWorld.getInstance().getRoundActiveState() ||
                GameWorld.GAME_STATE_T.ROUND_COMPLETE_WAIT == GameWorld.getInstance().getRoundActiveState()) {

            if (!GameWorld.getInstance().getIsPaused()) {
                GameWorld.getInstance().setIsPaused(true);
            } else {
                GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_QUIT);
            }
        }
    }

    protected void onMenuEvent() { // mt
    }

    /**
     * Display text string with a timed fadeout
     *
     * @param message text string
     * @param time    fadeout time
     */
    void setMsgLabel(String message, int time) {
        msgLabelCounter = time * 60;
        mesgLabel.setText(message);
        mesgLabel.setVisible(true);
    }

    private void updateGetInputs() {

        int checkedBox = 0; // button default at top selection

        mapper.latchInputState();

        if (mapper.isInputState(InputMapper.InputState.INP_FIRE1)) {

            if (GameWorld.getInstance().getIsPaused()) {
                onPauseEvent();
            } else {
                if (GameWorld.GAME_STATE_T.ROUND_OVER_MORTE == GameWorld.getInstance().getRoundActiveState()) {
                    GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_RESTART);
                } else {
                    onSelectEvent(); // so it can be overriden
                }
            }
        } else if (mapper.isInputState(InputMapper.InputState.INP_MENU)) {
            onEscEvent();

        } else if (mapper.isInputState(InputMapper.InputState.INP_VIEW)) {
            onCameraSwitch();

        } else if (mapper.isInputState(InputMapper.InputState.INP_SEL1)) {
            onMenuEvent();
        }
        if (GameWorld.getInstance().getIsPaused()) {
            checkedBox = checkedUpDown(mapper.getDpad().getY());
        }
        setCheckedBox(checkedBox);
    }

    /*
     * collect all the screen transition state management here
     * GameWorld ShowScreen() limited to reference in here!
     */
    private void updateScreenTransition() {

        switch (GameWorld.getInstance().getRoundActiveState()) {

            case ROUND_ACTIVE:
                if (getPrizeCount() >= SceneLoader.getNumberOfCrapiums()) {
                    GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_COMPLETE_WAIT);
                } else if (screenTimer <= 0) {
                    screenTimer = 2 * 60; // FPS // screen transition: 2 seconds fadeout
                    GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_TIMEOUT);
                }
                break;
            case ROUND_OVER_TIMEOUT:
                if (screenTimer <= 0) {
                    GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_QUIT);
                }
                break;
            case ROUND_COMPLETE_WAIT:
                if (screenTimer <= 0) {
                    screenTimer = 2 * 60; // FPS // screen transition: 2 seconds fadeout
                    GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_TIMEOUT);
                }
                if (canExit) { // exit sensor is tripped
                    GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_COMPLETE_NEXT);
                }
                break;
            case ROUND_OVER_MORTE: // Continue to Restart transition is triggered by hit "Select" while in Continue State
                if (screenTimer <= continueScreenTimeUp) {
                    screenTimer = 2 * 60; // FPS // screen transition: 2 seconds fadeout
                    GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_TIMEOUT);
                }
                break;
            case ROUND_COMPLETE_NEXT: // this state may be slightly superfluous
                GameWorld.getInstance().showScreen(new MainMenuScreen()); // tmp menu screen
                break;
            case ROUND_OVER_QUIT:
                GameWorld.getInstance().showScreen(); // goes to the default screen (startup splash/load)
                break;
            case ROUND_OVER_RESTART:
            default:
                break;
        }
    }

    private void showOSC(boolean show) {
// todo: put on screen controls in a table layout
        if (!GameWorld.getInstance().getIsTouchScreen()) {
            show = false;
        }
        touchpad.setVisible(show);
        xButton.setVisible(show);
        picButton.setVisible(show);
    }

    private void showPauseMenu(boolean show) {
// about 80% of the time, these are opposite to each other (menu goes up, on-screen-display down).
        onscreenMenuTbl.setVisible(show);
        playerInfoTbl.setVisible(!show);
    }

    private void updateUI() {

        setOverlayColor(0, 0, 0, 0);
        showOSC(false);
        showPauseMenu(false);
        GameWorld.GAME_STATE_T ras = GameWorld.getInstance().getRoundActiveState();

        updateTimerLbl();

        if (msgLabelCounter > 0) {
            msgLabelCounter -= 1;
        } else {
            mesgLabel.setVisible(false);
        }
        if (GameWorld.GAME_STATE_T.ROUND_OVER_MORTE == ras) {
            msgLabelCounter = 999;
            stringBuilder.setLength(0);
            mesgLabel.setText(stringBuilder.append("Continue? ").append((screenTimer - continueScreenTimeUp) / 60)); // FPS
            mesgLabel.setVisible(true);
            setOverlayColor(1, 0, 0, 0.5f); // red overlay

            // hackity hack  this is presently only means of generating "SELECT" event on touchscreen
            picButton.setVisible(GameWorld.getInstance().getIsTouchScreen());

        } else if (GameWorld.GAME_STATE_T.ROUND_COMPLETE_WAIT == ras) {

            setLabelColor(itemsLabel, Color.GREEN);
            stringBuilder.setLength(0);
            itemsLabel.setText(stringBuilder.append("EXIT"));

            stringBuilder.setLength(0);
            scoreLabel.setText(stringBuilder.append(getScore())); // update score indicator

        } else if (GameWorld.GAME_STATE_T.ROUND_ACTIVE == ras) {

            stringBuilder.setLength(0);
            itemsLabel.setText(
                    stringBuilder.append(
                            getPrizeCount()).append(" / ").append(SceneLoader.getNumberOfCrapiums()));

            stringBuilder.setLength(0);
            scoreLabel.setText(stringBuilder.append(getScore())); // update score indicator

        } else if (GameWorld.GAME_STATE_T.ROUND_OVER_TIMEOUT == ras) {

            playerInfoTbl.setVisible(false);
            fadeScreen();
        }
        if (GameWorld.getInstance().getIsPaused()) {
            setOverlayColor(0, 0, 1, 0.5f);
            showPauseMenu(true);
        } else if (GameWorld.getInstance().getIsTouchScreen()) {
            showOSC(true);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        updateGetInputs();
        updateScreenTransition();
        updateUI();
    }

    /**
     * Stage must be disposed by owning Screen
     */
    @Override
    public void dispose() {

        super.dispose();

        if (null != touchpadSkin)
            touchpadSkin.dispose();

        if (null != tpBackgnd)
            tpBackgnd.dispose();

        if (null != tpKnob)
            tpKnob.dispose();
    }
}
