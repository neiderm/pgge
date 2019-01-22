package com.mygdx.game.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.screens.GameWorld;
import com.mygdx.game.screens.MainMenuScreen;

import java.util.Arrays;

import static java.lang.Math.abs;

/**
 * Created by neiderm on 5/17/2018.
 */

public class PlayerCharacter extends Stage {

    private static final int KEY_CODE_POV_UP = Input.Keys.DPAD_UP;
    private static final int KEY_CODE_POV_DOWN = Input.Keys.DPAD_DOWN;
    private static final int KEY_CODE_POV_LEFT = Input.Keys.DPAD_LEFT;
    private static final int KEY_CODE_POV_RIGHT = Input.Keys.DPAD_RIGHT;
    private static final int KEY_CODE_ESC = Input.Keys.ESCAPE;
    private static final int KEY_CODE_BACK = Input.Keys.BACK;

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
      ... where "InputMapping"  should be array of Buttons-Inputs needed for the screen\
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
                    final float DZ = 0.25f; // actual number is irrelevant if < deadzoneRadius of TouchPad
                    float knobX = t.getKnobPercentX();
                    float knobY = t.getKnobPercentY();
                    Arrays.fill(axes, 0);

                    if (knobX < -DZ) {
                        axes[0] = -1;
                    } else if (knobX > DZ) {
                        axes[0] = +1;
                    }

                    if (knobY > DZ) {
                        axes[1] = -1;
                    } else if (knobY < -DZ) {
                        axes[1] = +1;
                    }

                    mapper.setAxis(-1, axes);
                }
            });   // user tapped in on screen

            Pixmap.setBlending(Pixmap.Blending.None);
            Pixmap button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
            button.setColor(1, 1, 1, .3f);
            button.fillCircle(25, 25, 25);

            addInputListener(
                    new InputListener() {
                        @Override
                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                            // "Circle" button
                            mapper.setInputState(InputStruct.InputState.INP_JUMP);
                            return false;
                        }},
                    button, 3 * Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 9f);
            button.dispose();
        }

///////////
// mapping each passed in function callback to an onscreen control, keyboard, gamepad etc.
        // how to order the InputListener Array
////////////
        if (null != buttonListeners) {
            for (InputListener listenr : buttonListeners) {
//                 this.cameraSwitchListener = listenr;    ////////// asdlfkjasdf;lkjasdf;lkjsadf;ksadf;klj
            }
        }

        initController();
    }

    @Override
    public boolean keyDown(int keycode) {
//        super.keyDown(keycode); // "ESC" || "BACK" -> MainMenuScreen
        /*
         Android "BACK" (on-screen btn) not handled by libGdx framework and seemingly no
         equivalent on PC keyboard ...
         "ESC" no equivalent on Android/emulator, so map them together.
         */
        if (KEY_CODE_ESC == keycode || KEY_CODE_BACK == keycode) {
// TODO: make this pause, and option RESUME/RESTART/QUIT

            if (true/*tmp*/) {
                GameWorld.getInstance().showScreen(new MainMenuScreen());

            } else {
////                isPaused = GameWorld.getInstance().getIsPaused();
//                if (!isPaused) {
////                    GameWorld.getInstance().setIsPaused(true);
//                    gameEventSignal.dispatch(gameEvent.set(IS_PAUSED, null, 0));
//                    isPaused = true;
//                }
//                else {
////                    GameWorld.getInstance().setIsPaused(false);
//                    gameEventSignal.dispatch(gameEvent.set(IS_UNPAUSED, null, 0));
//                    isPaused = false;
//                }
            }
        }

        int axisIndex = -1; // idfk
//        Arrays.fill(axes, 0);
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

        mapper.setAxis(axisIndex, axes);

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


    /*
    https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/extensions/ControllersTest.java
     */

    private void print(String message) {
        Gdx.app.log("Input", message);
    }

    private void initController() {

        // print the currently connected controllers to the console
        print("Controllers: " + Controllers.getControllers().size);
        int i = 0;
        for (Controller controller : Controllers.getControllers()) {
            print("#" + i++ + ": " + controller.getName());
        }
        if (Controllers.getControllers().size == 0)
            print("No controllers attached");

// hmmmmm when can I clearListeners?
        Controllers.addListener(new ControllerListenerAdapter() {

            //public
            int indexOf(Controller controller) {
                return Controllers.getControllers().indexOf(controller, true);
            }

            @Override
            public boolean buttonDown(Controller controller, int buttonIndex) {
                print("#" + indexOf(controller) + ", button " + buttonIndex + " down");

                final int BUTTON_CODE_3 = 3; // to be assigned by UI configuration ;)
//                if (BUTTON_CODE_8 == buttonIndex) {
//idfk                    cameraSwitchListener.touchDown(null, 0, 0, 0, 0);
                return false;
            }

            @Override
            public boolean buttonUp(Controller controller, int buttonIndex) {
                print("#" + indexOf(controller) + ", button " + buttonIndex + " up");
                return false;
            }

            @Override
            public boolean axisMoved(Controller controller, int axisIndex, float value) {
                /*          -1.0
                       -1.0   +   +1.0  (0)
                            + 1.0        */
                final float DZ = 0.25f; // actual number is irrelevant if < deadzoneRadius of TouchPad

                for (int idx = 0; idx < 4; idx++) {

                    float tmp = controller.getAxis(idx);

                    if (abs(tmp) < DZ)
                        tmp = 0; // inside deadzone

                    axes[idx] = tmp;
                }

                mapper.setAxis(axisIndex, axes);

                print("#" + indexOf(controller) + ", axes " + axisIndex + ": " + value);

                return false;
            }

            @Override
            public boolean povMoved(Controller controller, int povIndex, PovDirection value) {
                print("#" + indexOf(controller) + ", pov " + povIndex + ": " + value);

                Arrays.fill(axes, 0);

                if (value == PovDirection.west || value == PovDirection.southWest || value == PovDirection.northWest) {
                    axes[0] = -1;
                }
                if (value == PovDirection.east || value == PovDirection.southEast || value == PovDirection.northEast) {
                    axes[0] = +1;
                }
                if (value == PovDirection.north || value == PovDirection.northWest || value == PovDirection.northEast) {
                    axes[1] = -1;
                }
                if (value == PovDirection.south || value == PovDirection.southWest || value == PovDirection.southEast) {
                    axes[1] = +1;
                }

                mapper.setAxis(-1, axes);
                print("#" + indexOf(controller) + ", axes " + axes[0] + ": " + axes[1]);

                return false;
            }
        });
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

    public void addInputListener(InputListener listener, Pixmap pixmap, float x, float y) {

// TODO: texture neeeds to be dispose'd
        Texture myTexture = new Texture(pixmap);
        TextureRegion myTextureRegion = new TextureRegion(myTexture);
        TextureRegionDrawable myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);

        ImageButton button = new ImageButton(myTexRegionDrawable);
        button.setPosition(x, y);

        if (null != listener)
            button.addListener(listener);

        this.addActor(button);
    }

}
