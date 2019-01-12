package com.mygdx.game.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.screens.GameWorld;
import com.mygdx.game.screens.IUserInterface;
import com.mygdx.game.screens.MainMenuScreen;

import java.util.Arrays;

import static java.lang.Math.abs;

/**
 * Created by utf1247 on 5/17/2018.
 * This adapter has become specific to "tank" vehicle, it provides multiple controls - keyboard, touch screen, dpad etc.
 */

public class PlayerCharacter extends IUserInterface {
    /*
     * keys to be assigned by UI configuration ;)
     */
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
    public PlayerCharacter(InputStruct mapper, Array<InputListener> buttonListeners) {

        this.mapper = mapper;

// UI pixmaps etc. should eventually come from a user-selectable skin
        if (GameWorld.getInstance().getIsTouchScreen()) {
            addChangeListener(touchPadChangeListener);   // user tapped in on screen

            Pixmap button;
            Pixmap.setBlending(Pixmap.Blending.None);
            button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
            button.setColor(1, 1, 1, .3f);
            button.fillCircle(25, 25, 25);
            addInputListener(jumpButtonListener, button,
                    3 * Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 9f);
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


    private final ChangeListener touchPadChangeListener = new ChangeListener() {
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
    };

    private final InputListener jumpButtonListener = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

            // "Circle" button
            mapper.buttonSet(InputStruct.ButtonsEnum.BUTTON_1, 1, false);
            return false;
        }
    };

    @Override
    public boolean keyDown(int keycode) {
/*
        String message = new String ();
        message = String.format( "key Down: keycode = %d", keycode );
        Gdx.app.log("Input", message);
*/
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


        // TODO: for simple key presses, lookup table of Input.Keys-BUTTON_CODE
// build in a flag for "key held/isRepeated? "
        if (Input.Keys.SPACE == keycode) {
            // "Circle" button
            mapper.buttonSet(InputStruct.ButtonsEnum.BUTTON_1, 1, false);
        }
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

        // setup the listener that prints events to the console
        Controllers.addListener(new ControllerListenerAdapter() {

            //public
            int indexOf(Controller controller) {
                return Controllers.getControllers().indexOf(controller, true);
            }

            @Override
            public boolean buttonDown(Controller controller, int buttonIndex) {
                print("#" + indexOf(controller) + ", button " + buttonIndex + " down");

                final int BUTTON_CODE_8 = 8; // to be assigned by UI configuration ;)
                final int BUTTON_CODE_3 = 3; // to be assigned by UI configuration ;)

                if (BUTTON_CODE_8 == buttonIndex) {
//idfk                    cameraSwitchListener.touchDown(null, 0, 0, 0, 0);
                }

//                if (BUTTON_CODE_10 == buttonIndex) {  //  "Pause Resume-Restart-Quit"
                // code gets duplicated here between Controller handler and keyboard/touch handler because we don't have good AL!
                //   GameWorld.getInstance().showScreen(new MainMenuScreen());     /// create "back" event, for now. eventually map this to pause

                if (BUTTON_CODE_3 == buttonIndex)
                    // "Triangle" button .........................
                    // .................... ButtonsEnum is wrong then, this is a lousy AL!
                    mapper.buttonSet(InputStruct.ButtonsEnum.BUTTON_1, 1, false);

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
//                print("#" + indexOf(controller) + ", pov " + povIndex + ": " + value);

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

                return false;
            }
        });
    }
}
