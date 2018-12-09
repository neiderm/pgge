package com.mygdx.game.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.controllers.SteeringEntity;
import com.mygdx.game.screens.IUserInterface;

/**
 * Created by utf1247 on 5/17/2018.
 * This adapter has become specific to "tank" vehicle, it provides multiple controls - keyboard, touch screen, dpad etc.
 */

public class PlayerCharacter extends IUserInterface {

    // caller passes references to input listeners to be mapped to appropriate "buttons" - some will be UI functions
    // handled in here, or subsystem controls e.g. dpad controls go to tank steering, function buttons go to
    // guided missile susbsystem, cannon button inputs go to cannon etc.
    // maybe do a controller abstraction?
    // https://gist.github.com/nhydock/dc0501f34f89686ddf34
    // http://kennycason.com/posts/2015-12-27-libgdx-controller.html

    InputListener buttonBListener;

    public PlayerCharacter(final btRigidBody btRigidBodyPlayer, SteeringEntity steerable, Array<InputListener> buttonListeners) {

        // why must we pass a ridig body .... how about a transform?

        final PlayerInput<Vector3> playerInpSB = new PlayerInput<Vector3>(steerable, io, btRigidBodyPlayer);
        steerable.setSteeringBehavior(playerInpSB);

// UI pixmaps etc. should eventually come from a user-selectable skin
        addChangeListener(touchPadChangeListener);

        Pixmap button;

        Pixmap.setBlending(Pixmap.Blending.None);
        button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillCircle(25, 25, 25);
        addInputListener(actionButtonListener, button,
                3 * Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 9f);



///////////
// mapping each passed in function callback to an onscreen control, keyboard, gamepad etc.
        // how to order the InputListener Array
////////////
        for (InputListener listenr : buttonListeners){
            this.buttonBListener = listenr;    ////////// asdlfkjasdf;lkjasdf;lkjsadf;ksadf;klj
        }


        Pixmap.setBlending(Pixmap.Blending.None);
        button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillCircle(25, 25, 25);

        addInputListener(buttonBListener, button,
                (2 * Gdx.graphics.getWidth() / 4f), (Gdx.graphics.getHeight() / 9f));
//////////////


        button.dispose();

        initController();
    }


    /* need this persistent since we pass it every time but only update on change */
    private Vector3 inpVect = new Vector3(0f, 0f, 0f);
    private InputStruct io = new InputStruct();

    private final ChangeListener touchPadChangeListener = new ChangeListener() {
        @Override
        public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                /*          -1.0
                       -1.0   +   +1.0
                            + 1.0        */
            Touchpad t = (Touchpad) actor;

            final float DZ = 0.25f; // actual number is irrelevant if < deadzoneRadius of TouchPad

            // rotate by a constant rate according to stick left or stick right.
            float angularD = 0f;
            float linearD = 0f;

            float knobX = t.getKnobPercentX();
            float knobY = t.getKnobPercentY();

            if (knobX < -DZ) {
                angularD = 1f;  // left (ccw)
            } else if (knobX > DZ) {
                angularD = -1f;   // right (cw)
            }

            if (knobY > DZ) {
                linearD = -1f;
            } else if (knobY < -DZ) {
                // reverse thrust & "steer" opposite direction !
                linearD = +1f;
                angularD = -angularD;  // <---- note negative sign
            }
            // else ... inside deadzone

            io.set(inpVect.set(angularD, 0f, linearD), InputStruct.ButtonsEnum.BUTTON_NONE);
        }
    };

    private final InputListener actionButtonListener = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            io.set(inpVect.set(0f, 0f, 0f), InputStruct.ButtonsEnum.BUTTON_C);
            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) { /* empty */ }
    };


    private float angularDirection = 0f;
    private float linearDirection = 0f;

    @Override
    public boolean keyDown(int keycode) {
// this needs work ... then we can refactor
        super.keyDown(keycode);
        /*
         * steering rotate by a constant rate according to stick left or stick right.
         */
        if (Input.Keys.A == keycode ) {
            angularDirection = 1f;   // left
        }
        if (Input.Keys.D == keycode ) {
            angularDirection = -1f;
        }

        if (Input.Keys.W == keycode ) {
            linearDirection = -1f;
        }
        if (Input.Keys.S == keycode ) {
            // reverse thrust !
            linearDirection = +1f;
        }

        float tmpAngularDir = angularDirection;
        if (Gdx.input.isKeyPressed(Input.Keys.S) /* linearDirection > 0 */){
            // reverse thrust so "steer" opposite direction !
            tmpAngularDir = -angularDirection;
        }

        io.set(inpVect.set(
                tmpAngularDir, 0f, linearDirection), InputStruct.ButtonsEnum.BUTTON_NONE);

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {

        if (Input.Keys.A == keycode && !Gdx.input.isKeyPressed(Input.Keys.D) /* +1 == angularDirection */) {
            angularDirection = 0;
        }
        if (Input.Keys.D == keycode && !Gdx.input.isKeyPressed(Input.Keys.A) /* -1 == angularDirection */) {
            angularDirection = 0;
        }

        if (Input.Keys.W == keycode && !Gdx.input.isKeyPressed(Input.Keys.S) /* -1 == linearDirection */) {
            linearDirection = 0;
        }
        if (Input.Keys.S == keycode && !Gdx.input.isKeyPressed(Input.Keys.W) /* +1 == linearDirection */) {
            linearDirection = 0;
        }

        io.set(inpVect.set(
                angularDirection, 0f, linearDirection), InputStruct.ButtonsEnum.BUTTON_NONE);

        return false;
    }


    void print (String message) {
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

            public int indexOf (Controller controller) {
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

if (8 == buttonIndex)
    buttonBListener.touchDown(null, 0, 0, 0, 0);

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

                // rotate by a constant rate according to stick left or stick right.
                float angularD = 0f;
                float linearD = 0f;

                float knobX = controller.getAxis(0);
                float knobY = -controller.getAxis(1);  //   <---- note negative sign

                if (knobX < -DZ) {
                    angularD = 1f;  // left (ccw)
                } else if (knobX > DZ) {
                    angularD = -1f;   // right (cw)
                }

                if (knobY > DZ) {
                    linearD = -1f;
                } else if (knobY < -DZ) {
                    // reverse thrust & "steer" opposite direction !
                    linearD = +1f;
                    angularD = -angularD;  //   <---- note negative sign
                }
                // else ... inside deadzone

                io.set(inpVect.set(angularD, 0f, linearD), InputStruct.ButtonsEnum.BUTTON_NONE);

                return false;
            }

            @Override
            public boolean povMoved (Controller controller, int povIndex, PovDirection value) {
                print("#" + indexOf(controller) + ", pov " + povIndex + ": " + value);

                float angularD = 0, linearD = 0;

                if (value ==  PovDirection.west || value ==  PovDirection.southWest || value ==  PovDirection.northWest) {
                    angularD = 1f;  // left (ccw)
                }
                if (value ==  PovDirection.east  || value ==  PovDirection.southEast  || value ==  PovDirection.northEast) {
                    angularD = -1f;   // right (cw)
                }

                if (value ==  PovDirection.north  || value ==  PovDirection.northWest || value ==  PovDirection.northEast){
                    linearD = -1f;
                }
                if (value ==  PovDirection.south  || value ==  PovDirection.southWest || value ==  PovDirection.southEast){
                    // reverse thrust & "steer" opposite direction !
                    linearD = +1f;
                    angularD *= -1f;
                }
                // else ... inside deadzone

                io.set(inpVect.set(angularD, 0f, linearD), InputStruct.ButtonsEnum.BUTTON_NONE);

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
