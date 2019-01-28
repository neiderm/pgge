package com.mygdx.game.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.screens.GameWorld;

/**
 * Created by neiderm on 5/17/2018.
 */

public class PlayerCharacter extends Stage {

    private static final int KEY_CODE_POV_UP = Input.Keys.DPAD_UP;
    private static final int KEY_CODE_POV_DOWN = Input.Keys.DPAD_DOWN;
    private static final int KEY_CODE_POV_LEFT = Input.Keys.DPAD_LEFT;
    private static final int KEY_CODE_POV_RIGHT = Input.Keys.DPAD_RIGHT;

    private float[] axes = new float[4];


    // caller passes references to input listeners to be mapped to appropriate "buttons" - some will be UI functions
    // handled in here, or subsystem controls e.g. dpad controls go to tank steering, function buttons go to
    // guided missile susbsystem, cannon button inputs go to cannon etc.
    // maybe do a controller abstraction?
    // https://gist.github.com/nhydock/dc0501f34f89686ddf34
    // http://kennycason.com/posts/2015-12-27-libgdx-controller.html

    private InputStruct mapper;

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
    public PlayerCharacter(final InputStruct mapper, Array<InputListener> buttonListeners) {

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
        }
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
        Skin touchpadSkin;
        Drawable touchBackground;

        //Create a touchpad skin
        touchpadSkin = new Skin();
        //Set background image
        touchpadSkin.add("touchBackground", new Texture("data/touchBackground.png"));
        //Set knob image
        touchpadSkin.add("touchKnob", new Texture("data/touchKnob.png"));
        //Create TouchPad Style
        touchpadStyle = new Touchpad.TouchpadStyle();
        //Create Drawable's from TouchPad skin
        touchBackground = touchpadSkin.getDrawable("touchBackground");

// https://stackoverflow.com/questions/27757944/libgdx-drawing-semi-transparent-circle-on-pixmap
        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap background = new Pixmap(200, 200, Pixmap.Format.RGBA8888);
        background.setColor(1, 1, 1, .2f);
        background.fillCircle(100, 100, 100);

        //Apply the Drawables to the TouchPad Style
//        touchpadStyle.background = touchBackground;
        touchpadStyle.background = new TextureRegionDrawable(new TextureRegion(new Texture(background)));
        touchpadStyle.knob = touchpadSkin.getDrawable("touchKnob");

        //Create new TouchPad with the created style
        Touchpad touchpad = new Touchpad(10, touchpadStyle);
        //setBounds(x,y,width,height)
        touchpad.setBounds(15, 15, 200, 200);

        // touchpad.addListener ... https://gamedev.stackexchange.com/questions/127733/libgdx-how-to-handle-touchpad-input/127937#127937
        touchpad.addListener(touchPadChangeListener);
        this.addActor(touchpad);

        background.dispose();
    }
}
