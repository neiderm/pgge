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
import com.badlogic.gdx.scenes.scene2d.ui.Button;
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

    private final int gsBTNwidth =  Gdx.graphics.getHeight() * 3 / 8;
    private final int gsBTNheight =  Gdx.graphics.getHeight() * 3 / 8;
    private final int gsBTNx = Gdx.graphics.getWidth() / 2 - gsBTNwidth /2;
    private final int gsBTNy = Gdx.graphics.getHeight() / 2;
    private Vector2 v2 = new Vector2();
    private float[] axes = new float[4];


    // caller passes references to input listeners to be mapped to appropriate "buttons" - some will be UI functions
    // handled in here, or subsystem controls e.g. dpad controls go to tank steering, function buttons go to
    // guided missile susbsystem, cannon button inputs go to cannon etc.
    // maybe do a controller abstraction?
    // https://gist.github.com/nhydock/dc0501f34f89686ddf34
    // http://kennycason.com/posts/2015-12-27-libgdx-controller.html

    /*
     TODO: Array<InputListener> buttonListeners should be something like "Array<InputMapping> buttonListeners"
      ... where "InputMapping"  should be array of Buttons-Inputs needed for the screen
      {
        CONTROL_ID   //   POV_UP, POV_DOWN, BTN_START, BTN_X, BTN_DELTA,
        InputListener listener
        Button button
      }
      if listener==null then we have already a default base listener
     */
    GameUI(final InputMapper mapper /* , Array<InputListener> buttonListeners */) {
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
                    axes[1] = t.getKnobPercentY() * ( -1 );     // negated
                    mapper.setAxis(-1, axes);
                }});

            setupOnscreenControls(mapper);
        }
        setupInGameMenu();
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

        //Create a touchpad skin
        touchpadSkin = new Skin();
        //Set background image
//        touchpadSkin.add("touchBackground", new Texture("data/touchBackground.png"));
        //Set knob image
        touchpadSkin.add("touchKnob", new Texture("data/touchKnob.png"));
        //Create TouchPad Style
        touchpadStyle = new Touchpad.TouchpadStyle();
        //Create Drawable's from TouchPad skin
//        Drawable touchBackground = touchpadSkin.getDrawable("touchBackground");

// https://stackoverflow.com/questions/27757944/libgdx-drawing-semi-transparent-circle-on-pixmap
        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap background = new Pixmap(200, 200, Pixmap.Format.RGBA8888);
        background.setColor(1, 1, 1, .2f);
        background.fillCircle(100, 100, 100);

        //Apply the Drawables to the TouchPad Style
//        touchpadStyle.background = touchBackground;
        touchpadStyle.background = new TextureRegionDrawable(new TextureRegion(new Texture(background)));
        touchpadStyle.knob = touchpadSkin.getDrawable("touchKnob");
        // touchpadStyle.knob = = new TextureRegionDrawable ....
                //Create new TouchPad with the created style
        touchpad = new Touchpad(10, touchpadStyle);
        //setBounds(x,y,width,height)
        touchpad.setBounds(15, 15, 200, 200);

        // touchpad.addListener ... https://gamedev.stackexchange.com/questions/127733/libgdx-how-to-handle-touchpad-input/127937#127937
        touchpad.addListener(touchPadChangeListener);
        this.addActor(touchpad);

        background.dispose();
    }


    private void setupInGameMenu() {

        addButton("Resume");
        addButton("Restart");
        addButton("Quit");
        addButton("Camera");

        //        if (GameWorld.getInstance().getIsTouchScreen())
            addNextButton();
    }

    private Button nextButton;

    private void setupOnscreenControls(final InputMapper mapper){

        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap pixmap;

        pixmap = new Pixmap(gsBTNwidth, gsBTNheight, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, .3f);
        pixmap.drawRectangle(0, 0, gsBTNwidth, gsBTNheight);
        gsTexture = new Texture(pixmap);

        picButton = addImageButton(gsTexture, gsBTNx, gsBTNy,
                new InputListener(){
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        // alternatively ?  e.g. toScrnCoord.x = Gdx.input.getX() etc.
                        Vector2 toScrnCoord = picButton.localToParentCoordinates(v2.set(x, y));
                        mapper.setInputState(InputMapper.InputState.INP_SELECT, toScrnCoord.x, toScrnCoord.y);
                        return false;
                    }});
        pixmap.dispose();

        pixmap = new Pixmap(Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 4, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 1, 1, .3f);
        pixmap.drawRectangle(0, 0, Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 4);
        btnTexture = new Texture(pixmap);
        xButton = addImageButton(btnTexture, 3f * Gdx.graphics.getWidth() / 4, 0,
                new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        mapper.setInputState(InputMapper.InputState.INP_B2);
                        return false;
                    }});
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


    public void update(){

        boolean paused = GameWorld.getInstance().getIsPaused();

        if (null != touchpad) {
            touchpad.setVisible( ! paused );
        }
        if (null != xButton) {
            xButton.setVisible( ! paused );
        }
        if (null != picButton) {
            picButton.setVisible( ! paused );
        }

        if (!paused) {

            setCheckedBox( 0 ); // make sure button default at top selection on showing

            if (null != nextButton){
                // special sauce for touch screen
                nextButton.setChecked(false);
            }
            if (mapper.isInputState(InputMapper.InputState.INP_ESC)) {
                paused = true;
            }
        } else {

            if (mapper.isInputState(InputMapper.InputState.INP_SELECT)) {

                switch (getCheckedIndex()) {
                    default:
                    case 0: // resume
                        paused = false;
                        break;
                    case 1: // restart
//                    roundOver = true;
                        break;
                    case 2: // quit
                        break;
                    case 3:
                        paused = false;
                        break;
                }
                Gdx.app.log("GameUI", "  getCheckedIndex() == " + getCheckedIndex());
            }

            int checked = checkedUpDown(mapper.getDpad(null).getY()); // calls setCheckedBox

            setCheckedBox(checked);
        }
        GameWorld.getInstance().setIsPaused(paused);
    }

    @Override
    public void act (float delta) {

        super.act(delta);

        mapper.latchInputState();
        mapper.update(delta);

        update();
    }

    @Override
    public void dispose () {

        super.dispose();

        if (null != touchpadSkin)
            touchpadSkin.dispose();

        if (null != btnTexture)
            btnTexture.dispose();

        if (null != gsTexture)
            gsTexture.dispose();
    }
}
