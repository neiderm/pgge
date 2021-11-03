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
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
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

    private static final int DEFAULT_SCREEN_TIME = 60 * 60; // FPS
    private static final int TIME_LIMIT_WARN_SECS = 10;
    private static final int EIGHTH_SCREEN_WIDTH = GameWorld.VIRTUAL_HEIGHT / 8; // 1794/6=299
    private static final int TOUCHPAD_RADIUS = EIGHTH_SCREEN_WIDTH;

    private final Color hudOverlayColor;
    private final StringBuilder stringBuilder = new StringBuilder();
    private final Table playerInfoTbl = new Table();

    private ImageButton buttonA;
    private ImageButton buttonB;
    private Touchpad touchpad;
    private Label scoreLabel;
    private Label itemsLabel;
    private Label timerLabel;
    private Label mesgLabel;

    private int score; // my certain game the score resets every screen anyway so who cares
    private int screenTimer = DEFAULT_SCREEN_TIME;
    // disposable
    private Texture tpBackgnd;
    private Texture tpKnob;

    boolean canExit; // exit sensor is tripped
    int prizeCount;
    int continueScreenTimeUp;
    InputMultiplexer multiplexer;

    GameUI() {
        super();

        final int gsBTNwidth = GameWorld.VIRTUAL_WIDTH - (TOUCHPAD_RADIUS * 2);
        final int gsBTNheight = TOUCHPAD_RADIUS * 2;
        final int gsBTNx = TOUCHPAD_RADIUS * 2; // touch pad diameter is 2 * eighth
        final int gsBTNy = 0;

        // fills bottom of display right (or left..eventually) of gamepad
        buttonA = addImageButton(gsBTNx, gsBTNy, gsBTNwidth, gsBTNheight);
//, ButtonEventHandler.EVENT_A
        buttonA.addListener(
                new ActorGestureListener() {
                    @Override
                    public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, true);
                    }

                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                        mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_A, false);
                    }

                    @Override
                    public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
//                        if (Math.abs(deltaX) > Math.abs(deltaY))
                        {
                            final int THRSH = 5;
                            // r2l2active = true
                            if (deltaX < (-THRSH)) {
                                mapper.setAxis(InputMapper.VIRTUAL_R2_AXIS, 0);
                                mapper.setAxis(InputMapper.VIRTUAL_L2_AXIS, 1);
                            } else if (deltaX > (+THRSH)) {
                                mapper.setAxis(InputMapper.VIRTUAL_R2_AXIS, 1);
                                mapper.setAxis(InputMapper.VIRTUAL_L2_AXIS, 0);
                            }
                        }
                    }

                    /*
                    a fling could start a timer and apply the R2/L2 axes and then 0 the axis at time up
                     */
                    @Override
                    public void panStop(InputEvent event, float x, float y, int pointer, int button) {
                        // if r2l2active
                        mapper.setAxis(InputMapper.VIRTUAL_R2_AXIS, 0);
                        mapper.setAxis(InputMapper.VIRTUAL_L2_AXIS, 0);
                    }
                });

        // fills all/most of the screen above gamepad
        buttonB = addImageButton(0, TOUCHPAD_RADIUS * 2.0f,
                GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT - (TOUCHPAD_RADIUS * 2));
//                ButtonEventHandler.EVENT_B
        buttonB.addListener(
                new ActorGestureListener() {
                    // B is brake/flip/jump so there is no need to be too picky about discriminating it from
                    // other sources of touch down
                    @Override
                    public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_B, true);
                        // bpushed  = true;
                    }

                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                        // if (bpushed){
                        mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_B, false);
                    }

                    @Override
                    public void fling(InputEvent event, float velocityX, float velocityY, int button) {
                        if ((velocityX > 0.1f) && (velocityX > velocityY)) {
                            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_L1, true);
                        }
                    }

                    /*
                    try pan in X and Y direction for axes X2/Y2 (turret)
                     */
                    @Override
                    public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
//                        if (Math.abs(deltaX) > Math.abs(deltaY))
                        {
                            final int THRSH = 5;
                            // X1Y1active = true
                            if (deltaX < (-THRSH)) {
                                mapper.setAxis(InputMapper.VIRTUAL_X1_AXIS, -1);
                            } else if (deltaX > (+THRSH)) {
                                mapper.setAxis(InputMapper.VIRTUAL_X1_AXIS, +1);
                            }
                            if (deltaY < (-THRSH)) {
                                mapper.setAxis(InputMapper.VIRTUAL_Y1_AXIS, -1);
                            } else if (deltaY > (+THRSH)) {
                                mapper.setAxis(InputMapper.VIRTUAL_Y1_AXIS, +1);
                            }
                        }
                    }

                    @Override
                    public void panStop(InputEvent event, float x, float y, int pointer, int button) {
                        // if r2l2active
                        mapper.setAxis(InputMapper.VIRTUAL_X1_AXIS, 0);
                        mapper.setAxis(InputMapper.VIRTUAL_Y1_AXIS, 0);
                    }
                });

        createInfoTable();

        createMenu("Paused", "Resume", "Restart", "Quit", "Camera", "Debug Draw");

        hudOverlayColor = new Color(1, 1, 1, 0); // screen-fade overlay
        GameWorld.getInstance().setIsPaused(false); // default state for game-screen should be un-paused

        addTouchPad(TOUCHPAD_RADIUS, new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                Touchpad t = (Touchpad) actor;
                mapper.setAxis(0, t.getKnobPercentX());
                mapper.setAxis(1, t.getKnobPercentY() * (-1));
            }
        });

        multiplexer = new InputMultiplexer(this);
        Gdx.input.setInputProcessor(multiplexer);

        // anything else a sub-class needs to do can be overridden
        init();
    }

    /*
     * so it can be overridden
     */
    void init() { // mt
    }

    private void createInfoTable() {
        /// to playerUI
        scoreLabel = new Label("0000", uiSkin);
        playerInfoTbl.add(scoreLabel);

        itemsLabel = new Label("0/3", uiSkin);
        playerInfoTbl.add(itemsLabel);

        timerLabel = new Label("0:15", uiSkin);
        playerInfoTbl.add(timerLabel).padRight(1);

        playerInfoTbl.row().expand();

        mesgLabel = new Label("Continue? 9 ... ", uiSkin);
        playerInfoTbl.add(mesgLabel).colspan(3);
        mesgLabel.setVisible(false); // only see this in "Continue ..." screen

        playerInfoTbl.row().bottom().left();
        playerInfoTbl.setFillParent(true);
        playerInfoTbl.setVisible(false);
        addActor(playerInfoTbl);
//        playerInfoTbl.setDebug(true);
    }

    int getScreenTimer() {
        return screenTimer;
    }

    /**
     * Based on "http://www.bigerstaff.com/libgdx-touchpad-example" (link broken)
     * see also
     * https://stackoverflow.com/questions/27757944/libgdx-drawing-semi-transparent-circle-on-pixmap
     * https://gamedev.stackexchange.com/questions/127733/libgdx-how-to-handle-touchpad-input/127937#127937
     */
    private void addTouchPad(int radius, ChangeListener touchPadChangeListener) {

        Touchpad.TouchpadStyle touchpadStyle;
        int knobRadius = radius / 3;

        Pixmap button = new Pixmap(knobRadius * 2, knobRadius * 2, Pixmap.Format.RGBA8888);
        button.setColor(1, 0, 0, 0.5f);
        button.fillCircle(knobRadius, knobRadius, knobRadius);
        tpKnob = new Texture(button);

        Pixmap background = new Pixmap(radius * 2, radius * 2, Pixmap.Format.RGBA8888);
        background.setColor(1, 1, 1, 0.2f);
        background.fillCircle(radius, radius, radius);
        tpBackgnd = new Texture(background);
        touchpadStyle = new Touchpad.TouchpadStyle();
        touchpadStyle.background = new TextureRegionDrawable(new TextureRegion(tpBackgnd));
        touchpadStyle.knob = new TextureRegionDrawable(new TextureRegion(tpKnob));
        touchpad = new Touchpad(10, touchpadStyle);
        //setBounds(x,y,width,height)
        touchpad.setBounds(15, 15, radius * 2.0f, radius * 2.0f);
        touchpad.addListener(touchPadChangeListener);
        addActor(touchpad);

        button.dispose();
        background.dispose();
    }

    /*
     * make simple outlined button and bind an input event handler to it
     */
    private ImageButton addImageButton(float btnX, float btnY, int btnWidth, int btnHeight) {
        Pixmap pixmap = new Pixmap(btnWidth, btnHeight, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, .3f);
        pixmap.drawRectangle(0, 0, btnWidth, btnHeight);
        // no need to keep ref to texture for disposal (InGameMenu keeps reference list for disposal)
        ImageButton button = addImageButton(new Texture(pixmap), btnX, btnY);
        pixmap.dispose();
        return button;
    }

    /**
     * https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
     * "Note that using a StringBuilder is highly recommended against string concatenation in your
     * render method. The StringBuilder will create less garbage, causing almost no hick-ups due to garbage collection."
     */
    private void updateTimerLbl() {

        int minutes = 0;
        int seconds = 0;

        // !onscreenMenuTbl.isVisible doesn't work here for some reason
        if (!GameWorld.getInstance().getIsPaused()) {
            screenTimer -= 1;
        }

        int screenTimerSecs = screenTimer / 60; // FPS
        if (screenTimerSecs > 0) {
            minutes = screenTimerSecs / 60;
            seconds = screenTimerSecs % 60;
        }
        stringBuilder.setLength(0);
        stringBuilder.append(String.format(
                Locale.ENGLISH, "%02d", minutes)).append(":").append(String.format(Locale.ENGLISH, "%02d", seconds));

        if (screenTimerSecs <= TIME_LIMIT_WARN_SECS) {
            setLabelColor(timerLabel, Color.RED);
        }
        timerLabel.setText(stringBuilder);
    }

    private void fadeScreen() {

        float step = -0.05f;
        float alphaStep = -step;

        if (hudOverlayColor.r > 0.1f) {
            hudOverlayColor.r += step;
        }
        if (hudOverlayColor.g > 0.1f) {
            hudOverlayColor.g += step;
        }
        if (hudOverlayColor.b > 0.1f) {
            hudOverlayColor.b += step;
        }
        if (hudOverlayColor.a < 1) {
            hudOverlayColor.a += alphaStep;
        }
        setOverlayColor(hudOverlayColor.r, hudOverlayColor.g, hudOverlayColor.b, hudOverlayColor.a);
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

    /**
     * Event handlers can be overridden by Game Screen
     */
    protected void onSwitchView() { // mt
    }

    protected void onL1MenuOpen() { // mt
    }

    private void onQuit() {
        GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_QUIT);
    }

    private void onRestart() {
        GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_RESTART);
    }

    protected void onInputX() {
        if (getMenuVisibility()) {
            onPausedMenuX();
        } else if (GameWorld.GAME_STATE_T.ROUND_OVER_KILLED == GameWorld.getInstance().getRoundActiveState()) {
            onRestart();
        }
    }

    // to be overridden
    void onPaused() {
        GameWorld.getInstance().setIsPaused(true);
    }

    // to be overridden
    void onUnPaused() {
        GameWorld.getInstance().setIsPaused(false);
    }

    private void onPausedMenuX() {
        // un-pause game state and invoke selected action
        onUnPaused();

        switch (getCheckedIndex()) {
            default:
            case 0: // resume
                break;
            case 1: // restart
                onRestart();
                break;
            case 2: // quit
                onQuit();
                break;
            case 3: // camera included in menu for accessibility on TS
                onSwitchView(); // Game Screen overrides this
                break;
            case 4: // debug draw
                BulletWorld.USE_DDBUG_DRAW = !BulletWorld.USE_DDBUG_DRAW;
                // has to reinitialize bullet world to set the flag
                onRestart();
                break;
        }
    }

    private void onInputEsc() {

        if (GameWorld.GAME_STATE_T.ROUND_ACTIVE == GameWorld.getInstance().getRoundActiveState() ||
                GameWorld.GAME_STATE_T.ROUND_COMPLETE_WAIT == GameWorld.getInstance().getRoundActiveState()) {
            // if the menu is already visible, ESC/return acts as Quit
            if (getMenuVisibility()) {
                onQuit();
            } else {
                onPaused();
            }
        }
    }

    /**
     * Display text string with a timed fadeout
     *
     * @param message message text string
     * @param timeout duration to display label
     */
    void setMsgLabel(String message, int timeout) {
        setMsgLabel(message);
        mesgLabel.addAction(Actions.sequence(Actions.delay(timeout), Actions.hide()));
        mesgLabel.setVisible(true);
    }

    private void setMsgLabel(String message) {
        mesgLabel.setText(message);
    }

    public enum InputState {
        INP_NONE,
        INP_SELECT,
        INP_START,
        INP_A,
        INP_B,
        INP_Y,
        INP_X,
        INP_L1,
        INP_L2
    }

    private InputState preInputState;
    private InputState incomingInputState;

    /**
     * Evaluate discrete inputs and return the enum id of the active input if any.
     * Available physical devices - which can include e.g. gamepad controller buttons, keyboard
     * input, as well as virtual buttons on the touch screen - are multiplexed into the various
     * discrete input abstractions.
     * If incoming input state has changed from previous value, then update with stored
     * input state and return it. If no change, returns NONE.
     * Touch screen input can be fired in from Stage but if the Screen is not using TS inputs thru
     * Stage then the caller will have to handle their own TS checking, e.g.:
     * isTouched = Gdx.input.isTouched(0);
     * todo: how Input.Keys.BACK generated in Android Q
     */
    private void updateDiscreteInputs() {

        InputState newInputState = incomingInputState;
        incomingInputState = InputState.INP_NONE; // unlatch the input state

        if (mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_A)) {
            newInputState = InputState.INP_A;
// unused
//        } else if (mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_B)) {
//            newInputState = InputState.INP_B;

        } else if (mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_START)) {
            newInputState = InputState.INP_START;

        } else if (mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_SELECT)) {
            newInputState = InputState.INP_SELECT;

        } else if (mapper.getControlButton(InputMapper.VirtualButtonCode.BTN_L1)) {
            newInputState = InputState.INP_L1;
            mapper.setControlButton(InputMapper.VirtualButtonCode.BTN_L1, false); // for gesture input
        }

        InputState debouncedInputState = InputState.INP_NONE;
        if (preInputState != newInputState) { // debounce
            debouncedInputState = newInputState;
        }
        preInputState = newInputState;

        if ((InputState.INP_A == debouncedInputState)) {
            onInputX();
        } else if (InputState.INP_START == debouncedInputState) {
            onInputEsc();
        } else if (InputState.INP_SELECT == debouncedInputState) {
            onSwitchView();
        } else if (InputState.INP_L1 == debouncedInputState) {
            onL1MenuOpen();
        }
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
            case ROUND_OVER_KILLED: // Continue to Restart transition is triggered by InputX while in Continue state
                if (screenTimer <= continueScreenTimeUp) {
                    screenTimer = 2 * 60; // FPS // screen transition: 2 seconds fadeout
                    GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_TIMEOUT);
                }
                break;
            default:
                break;
        }
    }

    private void showOSC(boolean show) {
        if (!GameWorld.getInstance().getIsTouchScreen()) {
            show = false;
        }
        touchpad.setVisible(show);
        buttonB.setVisible(show);
        buttonA.setVisible(show);
    }

    private void showPauseMenu(boolean show) {
        setMenuVisibility(show);
        playerInfoTbl.setVisible(!show);
    }

    private void updateUI() {

        setOverlayColor(0, 0, 0, 0);
        showOSC(false);
        showPauseMenu(false);
        updateTimerLbl();
        // text strings rebuilt each frame - see note on updateTimerLbl() re use of StringBuilder
        switch (GameWorld.getInstance().getRoundActiveState()) {
            case ROUND_OVER_KILLED:
                stringBuilder.setLength(0);
                mesgLabel.setText(stringBuilder.append("Continue? ").append((screenTimer - continueScreenTimeUp) / 60)); // FPS
                mesgLabel.setVisible(true);
                setOverlayColor(1, 0, 0, 0.5f); // red overlay
                // todo the pick button is apparently the only means of generating "SELECT" event on touchscreen?
                buttonA.setVisible(GameWorld.getInstance().getIsTouchScreen());
                break;
            case ROUND_COMPLETE_WAIT:
                setLabelColor(itemsLabel, Color.GREEN);
                stringBuilder.setLength(0);
                itemsLabel.setText(stringBuilder.append("EXIT"));
                stringBuilder.setLength(0);
                scoreLabel.setText(stringBuilder.append(getScore())); // update score indicator
                break;
            case ROUND_ACTIVE:
                stringBuilder.setLength(0);
                itemsLabel.setText(stringBuilder.append(
                        getPrizeCount()).append(" / ").append(SceneLoader.getNumberOfCrapiums()));
                stringBuilder.setLength(0);
                scoreLabel.setText(stringBuilder.append(getScore())); // update score indicator
                break;
            case ROUND_OVER_TIMEOUT:
                playerInfoTbl.setVisible(false);
                fadeScreen();
                break;
            case ROUND_COMPLETE_NEXT:
                GameWorld.getInstance().showScreen(new MainMenuScreen()); // tmp menu screen
                break;
            case ROUND_OVER_QUIT:
                GameWorld.getInstance().showScreen(); // goes to the default screen (startup splash/load)
                break;
            default:
                break;
        }
        if (GameWorld.getInstance().getIsPaused() /* onscreenMenuTbl.isVisible() NO! */) {
            setOverlayColor(0, 0, 1, 0.5f);
            showPauseMenu(true);
        } else if (GameWorld.getInstance().getIsTouchScreen()) {
            showOSC(true);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        updateDiscreteInputs();
        updateScreenTransition();
        updateUI();
    }

    /**
     * Stage must be disposed by owning Screen
     */
    @Override
    public void dispose() {

        super.dispose();

        if (null != tpBackgnd) {
            tpBackgnd.dispose();
        }
        if (null != tpKnob) {
            tpKnob.dispose();
        }
    }
}
