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
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ArrayMap;

import java.util.Arrays;

/**
 * Created by neiderm on 6/28/2018.
 */
/*
   Thanks to https://www.asciiart.eu/computers/joysticks
             https://www.asciiart.eu/computers/game-consoles
      _____________________________

Learning all about Controllers ... controllers have much variation in capabilities and codes
reported. Furthermore, any given controller may report different codes depending upon the host OS!

    -----------------------------

Belkin "Nostromo Nostromo n45 Dual Analog Gamepad"

 |     [5]                  [7]  |
 |     [4]                  [6]  |
    -----------------------------
 |     [U]
 | [L]  + [R]                   B4[3]      |
 |    [D]     [8][9][10]   B3[2]    B2[1]  |
 |                              B1[0]      |
         -1           -2
      -0 axes 0    -3 axes 3
          1            2

        L1 == Input.Buttons.FORWARD
        B4 == Input.Buttons.BACK
        B3 == Input.Buttons.MIDDLE
        B2 == Input.Buttons.RIGHT
        B1 == Input.Buttons.LEFT
      ESC=8 MOUSE=9 ENTER=10
    -----------------------------


IPEGA PG-9076  "Microsoft X-Box 360 pad"

 Axis==2       Axis==5
       4==L1         5==R1
                Y[3]
 6==Select   X[2]   B[1]
 7==Start       A[0]

LR Axis0    Axis3
UD Axis1    Axis4

       L1 == Input.Buttons.FORWARD
        Y == Input.Buttons.BACK
        X == Input.Buttons.MIDDLE
        B == Input.Buttons.RIGHT
        A == Input.Buttons.LEFT
    -----------------------------

MYGT MY-C04  "MYGT Controller"

 6==L1      7==R1
 4==L1      5==R1
                Y[0]
 8==Back     X[3]   B[1]
 9==Select      A[2]

LR Axis3    Axis1
UD Axis2    Axis0

       L1 == Input.Buttons.FORWARD
        X == Input.Buttons.BACK
        A == Input.Buttons.MIDDLE
        B == Input.Buttons.RIGHT
        Y == Input.Buttons.LEFT
    -----------------------------
*/

class InputMapper /* stageWithController extends stage ? */ {

    public enum InputState {
        INP_NONE,
        INP_SELECT,
        INP_ESC,
        INP_B2
    }

    private Controller connectedCtrl;
    private Vector2 pointer = new Vector2();
    private float[] axes = new float[4];
    private boolean[] buttons = new boolean[10 + 1];

    private InputState incomingInputState = InputState.INP_NONE;
    private InputState preInputState = InputState.INP_NONE;


    private ArrayMap<ButtonsEnum, ButtonData> buttonsTable = new ArrayMap<ButtonsEnum, ButtonData>();

    private InputMapper.ButtonData buttonsData = new InputMapper.ButtonData();


    InputMapper() {

        initController();
        connectedCtrl = getConnectedCtrl(0);
    }


    private class ButtonData {

        int value;
        boolean isRepeatable;

        ButtonData setValue(int value, boolean isRepeatable) {
            this.value = value;
            this.isRepeatable = isRepeatable;
            return this;
        }
    }


    private void buttonSet(ButtonsEnum key, int value, boolean isRepeated) {

        buttonsTable.put(key, buttonsData.setValue(value, isRepeated));
    }

    private enum ButtonsEnum { // idfk
        BUTTON_NONE,
        BUTTON_1,
        BUTTON_2,
        BUTTON_3,
        BUTTON_4,
        BUTTON_5,
        BUTTON_6,
        BUTTON_7,
        BUTTON_8,
        BUTTON_9,
        BUTTON_10
    }

    // get the "virtual axis"
    float getAxisX(int axisIndex) {

        return analogAxes.x;
    }

    // get the "virtual axis"
    float getAxisY(int axisIndex) {

        return analogAxes.y;
    }

    /*
      axisIndex: index of changed axes (only has value for a real axes?
      values[]: values of all 4 axes (only have all 4 if there are 2 analog mushrooms)
      Otherwise, virtual axes e.g. POV, WASD only have 2 axes.
                            -1.0
                       -1.0   +   +1.0
                            + 1.0
     */
    void setAxis(int axisIndex, float[] values) {

        // ie. analogAxes[axisIndex].x ....
        analogAxes.x = values[0];
        analogAxes.y = values[1];
    }

    private static Controller getConnectedCtrl(int selectControl) {
        // If a controller is connected, find it and grab a link to it
        Controller connectedCtrl = null;
        int i = 0;
        for (Controller c : Controllers.getControllers()) {
            if (i++ == selectControl) {
                connectedCtrl = c;
                break;
            }
        }
        return connectedCtrl;
    }

/*
    public class InputStateSt    {
        private InputState inputAction;
        private float x;
        private float y;
        private Vector2 xy = new Vector2();
        public InputState getInputAction() {
            return inputAction;
        }
        public void setInputAction(InputState inputAction) {
            this.inputAction = inputAction;
        }
        public Vector2 getXY() {
            return xy;
        }
        private void setXy(float x, float y) {
            xy.set(x, y);
        }
    }
    */


    private InputState evalNewInputState(boolean checkIsTouched) {

        InputState newInputState = incomingInputState;

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyPressed(Input.Keys.BACK)
                || getControlButton(Input.Buttons.FORWARD)) {

            newInputState = InputState.INP_ESC;

        } else if (Gdx.input.isKeyPressed(Input.Keys.SPACE) || getControlButton(Input.Buttons.LEFT)
                || (Gdx.input.justTouched() && checkIsTouched)) {

            newInputState = InputState.INP_SELECT;
            pointer.set(Gdx.graphics.getHeight() / 2f, Gdx.graphics.getHeight() / 2f); // default to screen center or whatever

        } else if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || getControlButton(Input.Buttons.RIGHT)) {

            newInputState = InputState.INP_B2;
        }

        return newInputState;
    }

    /*
     * Eval update and return the input state,
     * If incoming input state has changed from current (previous) value, then update with stored
     * input state and return it. If not change, returns NONE.
     * Since the global Incoming Input State value is always "de-latched" reset then this is only
     * useful in that situation that the caller needs the actual new input state value for further processing.
     * Keeping this interface for now just in case but should probably be using checkInputState().
     */
    InputState getInputState(boolean checkIsTouched) {

        InputState newInputState = evalNewInputState(checkIsTouched);

        InputState rv = InputState.INP_NONE;

        if (preInputState != newInputState) { // debounce
            rv = newInputState;
        }
        preInputState = newInputState;
        incomingInputState = InputState.INP_NONE; // unlatch the input state

        return rv;
    }

    InputState getInputState() {
        return getInputState(false);
    }

    private InputState nowInputState;

    void latchInputState(){
        nowInputState = getInputState();
    }
    void unlatchInputstate(){
        nowInputState = InputState.INP_NONE;
    }
    boolean isInputState(InputState inp){
        return (nowInputState == inp);
    }

    /*
     * Eval and update the input state,
     * Return true if state is changes AND matches the wanted state. Else return false.
     * Only resets the Incoming State if changes AND matches.
     * So this is useful for doing a series of tests for different wanted states where it is OK
     * (and necessary) that the incoming state is not reset/cleared.
     */
    boolean checkInputState(InputState wantedInputState) {

        InputState newInputState = evalNewInputState(true);

        boolean rv = false;

        if (newInputState == wantedInputState) {
            if (preInputState != newInputState) { // debounce
                rv = true;
                preInputState = newInputState;
            }
            incomingInputState = InputState.INP_NONE; // unlatch the input state
        }
        return rv;
    }
/*
    boolean checkInputState(InputState wantedInputState) {

        return checkInputState(wantedInputState, false);
    }
*/
    /*
     * sets the passed input state, pointer defaults to middle of screen if non-touchscreen system
     */
    void setInputState(InputState incomingInputState) {

        this.incomingInputState = incomingInputState;
    }

    void setPointer(float x, float y) {

        setInputState(InputState.INP_SELECT);
        pointer.set(x, y);
    }

    /*
    	public int getX () {
		return (int)(Mouse.getX() * Display.getPixelScaleFactor());
	}
     */
    float getPointerX() {
        return pointer.x;
    }

    /*
    	public int getY () {
		return Gdx.graphics.getHeight() - 1 - (int)(Mouse.getY() * Display.getPixelScaleFactor());
	}
     */
    float getPointerY() {
        return Gdx.graphics.getHeight() - pointer.y; // normalize this to the way libGdx does ;)
    }


    private boolean getControlButton(int button) {

        boolean rv = false;
        if (null != connectedCtrl) {
            rv = buttons[button];
        }
        return rv;
    }

    public class DpadAxis {
        int x;
        int y;

        Vector2 axes = new Vector2();

        Vector2 getAxes() {
            return axes.set(x, y);
        }

        void clear() {
            x = 0;
            y = 0;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    private class AnalogAxis {
        float x;
        float y;

        void clear() {
            x = 0;
            y = 0;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }

    // Vector2 ??
    private DpadAxis dPadAxes = new DpadAxis(); // typically only 1 dPad, but it could be implemented as either an axis or 4 buttons while libGdx has it's own abstraction
    private AnalogAxis analogAxes = new AnalogAxis(); // would need array of max analog axes, for now just use one

    /*
     * "virtual dPad" provider using either controller POV or keyboard U/D/L/R
     *
     * NOTE: Android emulator: it gets keyboard input surprisingly on Windows (but not Linux it seems).
     * But glitchy and not worth considering.
     */
    /* Vector2 */
    DpadAxis getDpad(DpadAxis asdf) {

        dPadAxes.clear();

        PovDirection povDir = PovDirection.center;
        if (null != connectedCtrl) {
            povDir = connectedCtrl.getPov(0);
        }
        /*
         e.g. povDirection.northeast, southeast etc. ignored. If previous POV direction were
         kept, then e.g.

         if (Gdx.input.isKeyPressed(Input.Keys.UP) || PovDirection.north == povDir ||
                   ( previousPovDirection.north == povDir && ( PovDirection.northEast == povDir || PovDirection.northWest == povDir  ) )  )
         */
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || PovDirection.north == povDir) {
            dPadAxes.y = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || PovDirection.south == povDir) {
            dPadAxes.y = 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || PovDirection.west == povDir) {
            dPadAxes.x = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || PovDirection.east == povDir) {
            dPadAxes.x = 1;
        }
        return dPadAxes;
    }


    /*
     https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/extensions/ControllersTest.java
    */

    private void print(String message) {
//        Gdx.app.log("Input", message);
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
        /*
         * keep the reference so the listener can be removed at dispose()    ?????????????
         *
         * This class doesn't have a 'dispose()' interface to put
         *         Controllers.removeListener(controllerListener);
         *
         *         but if it ends up being a base class of GameUI ...........
         */
        Controllers.addListener(new ControllerListenerAdapter() {

            int indexOf(Controller controller) {
                return Controllers.getControllers().indexOf(controller, true);
            }

            @Override
            public boolean buttonDown(Controller controller, int buttonIndex) {

                print("#" + indexOf(controller) + ", button " + buttonIndex + " down");

                if (buttonIndex > 10){
                    Gdx.app.log("InputMapper", "buttonIndex > 10 (" + buttonIndex + ")" );
                }
                buttons[buttonIndex] = true;
                return false;
            }

            @Override
            public boolean buttonUp(Controller controller, int buttonIndex) {
                print("#" + indexOf(controller) + ", button " + buttonIndex + " up");
                buttons[buttonIndex] = false;
                return false;
            }

            @Override
            public boolean axisMoved(Controller controller, int axisIndex, float value) {
                /*          -1.0
                       -1.0   +   +1.0  (0)
                            + 1.0        */
                for (int idx = 0; idx < 4; idx++) {
                    axes[idx] = controller.getAxis(idx);
                }
                setAxis(axisIndex, axes);
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

                setAxis(-1, axes);
                print("#" + indexOf(controller) + ", axes " + axes[0] + ": " + axes[1]);

                return false;
            }
        });
    }
}
