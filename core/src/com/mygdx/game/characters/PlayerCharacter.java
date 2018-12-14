package com.mygdx.game.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.controllers.SteeringEntity;
import com.mygdx.game.controllers.TankController;
import com.mygdx.game.screens.IUserInterface;

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

    private static final int BUTTON_CODE_1 = 1;
/*
    private static final int KEY_CODE_POV_UP    = Input.Keys.W;
    private static final int KEY_CODE_POV_DOWN  = Input.Keys.S;
    private static final int KEY_CODE_POV_LEFT  = Input.Keys.A;
    private static final int KEY_CODE_POV_RIGHT = Input.Keys.D;
    */
    private static final int KEY_CODE_POV_UP     = Input.Keys.DPAD_UP;
    private static final int KEY_CODE_POV_DOWN     = Input.Keys.DPAD_DOWN;
    private static final int KEY_CODE_POV_LEFT     = Input.Keys.DPAD_LEFT;
    private static final int KEY_CODE_POV_RIGHT     = Input.Keys.DPAD_RIGHT;

    private float [] axes = new float[4];


    // caller passes references to input listeners to be mapped to appropriate "buttons" - some will be UI functions
    // handled in here, or subsystem controls e.g. dpad controls go to tank steering, function buttons go to
    // guided missile susbsystem, cannon button inputs go to cannon etc.
    // maybe do a controller abstraction?
    // https://gist.github.com/nhydock/dc0501f34f89686ddf34
    // http://kennycason.com/posts/2015-12-27-libgdx-controller.html

    private InputListener cameraSwitchListener;

    public PlayerCharacter(
            SteeringEntity steerable, Array<InputListener> buttonListeners, TankController tc) {

        final PlayerInput<Vector3> playerInpSB = new PlayerInput<Vector3>( io, tc);
        steerable.setSteeringBehavior(playerInpSB);

// UI pixmaps etc. should eventually come from a user-selectable skin
        addChangeListener(touchPadChangeListener);

        Pixmap button;

        Pixmap.setBlending(Pixmap.Blending.None);
        button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillCircle(25, 25, 25);
        addInputListener(jumpButtonListener, button,
                3 * Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 9f);



///////////
// mapping each passed in function callback to an onscreen control, keyboard, gamepad etc.
        // how to order the InputListener Array
////////////
        for (InputListener listenr : buttonListeners){
            this.cameraSwitchListener = listenr;    ////////// asdlfkjasdf;lkjasdf;lkjsadf;ksadf;klj
        }


        Pixmap.setBlending(Pixmap.Blending.None);
        button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillCircle(25, 25, 25);

        addInputListener(cameraSwitchListener, button,
                (2 * Gdx.graphics.getWidth() / 4f), (Gdx.graphics.getHeight() / 9f));
//////////////


        button.dispose();

        initController();
    }


    /* need this persistent since we pass it every time but only update on change */
    private InputStruct io = new InputStruct();

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

            io.setAxis(-1, axes);
        }
    };

    private final InputListener jumpButtonListener = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

            io.buttonSet( BUTTON_CODE_1 /* InputStruct.ButtonsEnum.BUTTON_1 */ );
            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) { /* empty */ }
    };


    @Override
    public boolean keyDown(int keycode) {

        super.keyDown(keycode);

        int axisIndex = -1; // idfk
//        Arrays.fill(axes, 0);
        if (KEY_CODE_POV_LEFT == keycode ) {
            axes[0] = -1;
        }
        if (KEY_CODE_POV_RIGHT == keycode ) {
            axes[0] = +1;
        }
        if (KEY_CODE_POV_UP == keycode ) {
            axes[1] = -1;
        }
        if (KEY_CODE_POV_DOWN == keycode ) {
            axes[1] = +1;
        }

        if (Input.Keys.SPACE == keycode)
            io.buttonSet( BUTTON_CODE_1 );

        io.setAxis(axisIndex, axes);

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

        io.setAxis(axisIndex, axes);

        return false;
    }


    /*
    https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/extensions/ControllersTest.java
     */

    private void print (String message) {
        Gdx.app.log("Input", message);
    }

    private void initController() {

        // print the currently connected controllers to the console
        print("Controllers: " + Controllers.getControllers().size);
        int i = 0;
        for (Controller controller : Controllers.getControllers()) {
            print("#" + i++ + ": " + controller.getName());
        }
        if (Controllers.getControllers().size == 0) print("No controllers attached");

        // setup the listener that prints events to the console
        Controllers.addListener(new ControllerListener() {

            //public
            int indexOf (Controller controller) {
                return Controllers.getControllers().indexOf(controller, true);
            }

            @Override
            public void connected (Controller controller) {
                print("connected " + controller.getName());
                int i = 0;
                for (Controller c : Controllers.getControllers()) {
                    print("#" + i++ + ": " + c.getName());
                }
            }

            @Override
            public void disconnected (Controller controller) {
                print("disconnected " + controller.getName());
                int i = 0;
                for (Controller c : Controllers.getControllers()) {
                    print("#" + i++ + ": " + c.getName());
                }
                if (Controllers.getControllers().size == 0) print("No controllers attached");
            }

            @Override
            public boolean buttonDown (Controller controller, int buttonIndex) {
                print("#" + indexOf(controller) + ", button " + buttonIndex + " down");

                final int BUTTON_CODE_8 = 8; // to be assigned by UI configuration ;)

                if (BUTTON_CODE_8 == buttonIndex)
                    cameraSwitchListener.touchDown(null, 0, 0, 0, 0);

                io.buttonSet( buttonIndex );
                return false;
            }

            @Override
            public boolean buttonUp (Controller controller, int buttonIndex) {
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

                io.setAxis(axisIndex, axes);

                print("#" + indexOf(controller) + ", axes " + axisIndex + ": " + value);
//                print("[" + controller.getAxis(0) + ", " + controller.getAxis(1) + ", " + controller.getAxis(2) + ", " + controller.getAxis(3) +  "]");

                return false;
            }

            @Override
            public boolean povMoved (Controller controller, int povIndex, PovDirection value) {
//                print("#" + indexOf(controller) + ", pov " + povIndex + ": " + value);

                Arrays.fill(axes, 0);

                if (value ==  PovDirection.west || value ==  PovDirection.southWest || value ==  PovDirection.northWest) {
                    axes[0] = -1;
                }
                if (value ==  PovDirection.east  || value ==  PovDirection.southEast  || value ==  PovDirection.northEast) {
                    axes[0] = +1;
                }

                if (value ==  PovDirection.north  || value ==  PovDirection.northWest || value ==  PovDirection.northEast){
                    axes[1] = -1;
                }
                if (value ==  PovDirection.south  || value ==  PovDirection.southWest || value ==  PovDirection.southEast){
                    axes[1] = +1;
                }

                io.setAxis(-1, axes);

                return false;
            }

            @Override
            public boolean xSliderMoved (Controller controller, int sliderIndex, boolean value) {
                print("#" + indexOf(controller) + ", x slider " + sliderIndex + ": " + value);
                return false;
            }

            @Override
            public boolean ySliderMoved (Controller controller, int sliderIndex, boolean value) {
                print("#" + indexOf(controller) + ", y slider " + sliderIndex + ": " + value);
                return false;
            }

            @Override
            public boolean accelerometerMoved (Controller controller, int accelerometerIndex, Vector3 value) {
                // not printing this as we get to many values
                return false;
            }
        });
    }
}
