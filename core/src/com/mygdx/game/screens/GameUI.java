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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.game.GameWorld;

/**
 * Created by neiderm on 5/17/2018.
 */

public class GameUI extends InGameMenu {

    private static final int KEY_CODE_POV_UP = Input.Keys.DPAD_UP;
    private static final int KEY_CODE_POV_DOWN = Input.Keys.DPAD_DOWN;
    private static final int KEY_CODE_POV_LEFT = Input.Keys.DPAD_LEFT;
    private static final int KEY_CODE_POV_RIGHT = Input.Keys.DPAD_RIGHT;

    private ImageButton picButton;
    private ImageButton xButton;
    private Touchpad touchpad;
    // @dispoable
    private Skin touchpadSkin;
    private Texture gsTexture;
    private Texture btnTexture;
    private Texture tpBackgnd;
    private Texture tpKnob;

    private final int gsBTNwidth = Gdx.graphics.getHeight() * 3 / 8;
    private final int gsBTNheight = Gdx.graphics.getHeight() * 3 / 8;
    // placement relative to absolute center of screen ... i guess
    private final int gsBTNx = Gdx.graphics.getWidth() / 2 - gsBTNwidth / 2;
    private final int gsBTNy = Gdx.graphics.getHeight() / 2;

    private Vector2 v2 = new Vector2();
    private float[] axes = new float[4];


    GameUI(final InputMapper mapper) {
        //this.getViewport().getCamera().update(); // GN: hmmm I can get the camera

        super(null, "Paused");

        // hack ...assert default state for game-screen unpaused since use it as a visibility flag for on-screen menu!
        GameWorld.getInstance().setIsPaused(false);

        this.mapper = mapper;

        if (GameWorld.getInstance().getIsTouchScreen()) {

            addChangeListener(new ChangeListener() {
                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {

                    Touchpad t = (Touchpad) actor;
                    axes[0] = t.getKnobPercentX();
                    axes[1] = t.getKnobPercentY() * (-1);     // negated
                    mapper.setAxis(-1, axes);
                }
            });

            setupOnscreenControls(mapper);
        }

        setupInGameMenu();
        setupPlayerInfo();
    }

/*    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {

        Gdx.app.log("", "screenX = " + screenX + " screenY = " + screenY);
        return true;
    }*/

    @Override
    public boolean keyDown(int keycode) {

        int axisIndex = -1; // idfk

        if (KEY_CODE_POV_LEFT == keycode) {
            axes[0] = -1;
        }
        if (KEY_CODE_POV_RIGHT == keycode) {
            axes[0] = +1;
        }
        if (KEY_CODE_POV_UP == keycode) {
            axes[1] = -1;
        }
        if (KEY_CODE_POV_DOWN == keycode) {
            axes[1] = +1;
        }

        mapper.setAxis(255, axes);

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {

        int axisIndex = -1; // idfk

        if (KEY_CODE_POV_LEFT == keycode && !Gdx.input.isKeyPressed(KEY_CODE_POV_RIGHT) ||
                KEY_CODE_POV_RIGHT == keycode && !Gdx.input.isKeyPressed(KEY_CODE_POV_LEFT)) {
            axes[0] = 0;
            axisIndex = 0;
        }
        if (KEY_CODE_POV_UP == keycode && !Gdx.input.isKeyPressed(KEY_CODE_POV_DOWN) ||
                KEY_CODE_POV_DOWN == keycode && !Gdx.input.isKeyPressed(KEY_CODE_POV_UP)) {
            axes[1] = 0;
            axisIndex = 1;
        }

        mapper.setAxis(axisIndex, axes);

        return false;
    }

    /**
     * Based on "http://www.bigerstaff.com/libgdx-touchpad-example"
     */
    private void addChangeListener(ChangeListener touchPadChangeListener) {

        Touchpad.TouchpadStyle touchpadStyle;

        // these numbers have been complete arbitrary
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

        //Create TouchPad Style
        touchpadStyle = new Touchpad.TouchpadStyle();
        //Create Drawable's from TouchPad skin
//        Drawable touchBackground = touchpadSkin.getDrawable("touchBackground");

// https://stackoverflow.com/questions/27757944/libgdx-drawing-semi-transparent-circle-on-pixmap
        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap background = new Pixmap(tpRadius * 2, tpRadius * 2, Pixmap.Format.RGBA8888);
        background.setColor(1, 1, 1, .2f);
        background.fillCircle(tpRadius, tpRadius, tpRadius);

        //Apply the Drawables to the TouchPad Style
//        touchpadStyle.background = touchBackground;

        tpBackgnd = new Texture(background);
        touchpadStyle.background = new TextureRegionDrawable(new TextureRegion(tpBackgnd));
//        touchpadStyle.knob = touchpadSkin.getDrawable("touchKnob");
        touchpadStyle.knob = new TextureRegionDrawable(new TextureRegion(tpKnob));

        //Create new TouchPad with the created style
        touchpad = new Touchpad(10, touchpadStyle);
        //setBounds(x,y,width,height)
        touchpad.setBounds(15, 15, tpRadius * 2f, tpRadius * 2f);

        // touchpad.addListener ... https://gamedev.stackexchange.com/questions/127733/libgdx-how-to-handle-touchpad-input/127937#127937
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
    }

    private void setupOnscreenControls(final InputMapper mapper) {

        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap pixmap;

        pixmap = new Pixmap(gsBTNwidth, gsBTNheight, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, .3f);
        pixmap.drawRectangle(0, 0, gsBTNwidth, gsBTNheight);
        gsTexture = new Texture(pixmap);

        picButton = addImageButton(gsTexture, gsBTNx, gsBTNy,
                new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        // alternatively ?  e.g. toScrnCoord.x = Gdx.input.getX() etc.
                        Vector2 toScrnCoord = picButton.localToParentCoordinates(v2.set(x, y));
                        mapper.setPointer(toScrnCoord.x, toScrnCoord.y);
                        return false;
                    }
                });
        pixmap.dispose();

        pixmap = new Pixmap(Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 4, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, .3f);
        pixmap.drawRectangle(0, 0, Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 4);
        btnTexture = new Texture(pixmap);
// placement relative to absolute center of screen .. i guess
        xButton = addImageButton(btnTexture, 3f * Gdx.graphics.getWidth() / 4, 0,
                new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        mapper.setInputState(InputMapper.InputState.INP_B2);
                        return false;
                    }
                });
        pixmap.dispose();
    }

    // libGdx managedTextures ??
    private ImageButton addImageButton(Texture tex, float posX, float posY, EventListener listener) {

        TextureRegionDrawable myTexRegionDrawable = new TextureRegionDrawable(new TextureRegion(tex));
        ImageButton newButton = new ImageButton(myTexRegionDrawable);
        addActor(newButton);
        newButton.setPosition(posX, posY);
        newButton.addListener(listener); // ignored return value
        return newButton;
    }

    private void update(boolean menuActive) {

        if (null != touchpad) {
            touchpad.setVisible(!menuActive);
        }
        if (null != xButton) {
            xButton.setVisible(!menuActive);
        }
        if (null != picButton) {
            picButton.setVisible(!menuActive);
        }
    }

    @Override
    public void act(float delta) {

        super.act(delta);

        update(GameWorld.getInstance().getIsPaused()
                || GameWorld.GAME_STATE_T.ROUND_OVER_MORTE == GameWorld.getInstance().getRoundActiveState());

// hackity hack  this is presently only means of generating "SELECT" event on touchscreen
        if (GameWorld.GAME_STATE_T.ROUND_OVER_MORTE == GameWorld.getInstance().getRoundActiveState()) {

            if (null != picButton) {
                picButton.setVisible(true);
            }
        }
    }

    @Override
    public void dispose() {

        super.dispose();

        if (null != touchpadSkin)
            touchpadSkin.dispose();

        if (null != btnTexture)
            btnTexture.dispose();

        if (null != gsTexture)
            gsTexture.dispose();

        if (null != tpBackgnd)
            tpBackgnd.dispose();

        if (null != tpKnob)
            tpKnob.dispose();
    }
}