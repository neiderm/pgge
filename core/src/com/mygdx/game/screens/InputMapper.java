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
import com.mygdx.game.GameWorld;

import java.util.Arrays;

/**
 * Created by neiderm on 6/28/2018.
 */
/*

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

    -----------------------------

MYGT MY-C04  "MYGT Controller"

 6==L1      7==R1
 4==L1      5==R1
                Y[0]
 8==Back     X[3]   B[1]
 9==Select      A[2]

LR Axis3    Axis1
UD Axis2    Axis0
*/

class InputMapper {

    static int NumberControlCfgTypes;
    private final int MAX_AXES = 8;
    private final int MAX_BUTTONS = 256; // arbitrary size to fit range of button index space

    // so this is the control switches abstrction
    public enum InputState {
        INP_NONE,
        INP_START, // INP_SELEct gameUI
        INP_SELECT, //        INP_A,
        INP_ESC, // INP_START
        INP_B2 ;   // INP_B
    }
//    final InputState INP_SELECT = InputState.DIO_A;

    private VirtualButtons buttonmMapping[] = new VirtualButtons[MAX_BUTTONS];
    private boolean buttonStates[] = new boolean[VirtualButtons.values().length];

    private Controller connectedCtrl;
    private Vector2 pointer = new Vector2();

    private InputState incomingInputState = InputState.INP_NONE;
    private InputState preInputState = InputState.INP_NONE;


    InputMapper() {

        initController();
        connectedCtrl = getConnectedCtrl(0);

        // bah ... debounce this (bounce from key on previous screen)
        checkInputState(InputState.INP_SELECT); // seems to be necessary and effective with game pad ;)
    }

//    private class ButtonData {
//
//        int value;
//        boolean isRepeatable;
//
//        ButtonData setValue(int value, boolean isRepeatable) {
//            this.value = value;
//            this.isRepeatable = isRepeatable;
//            return this;
//        }
//    }

    private enum VirtualButtons {
        BTN_NONE,
        BTN_ESC, // how many PC game pads have a 3rd face button?
        BTN_SELECT, // BTN_MOUSE
        BTN_START, // BTN_ENTER
        BTN_A,
        BTN_B,
        BTN_C,
        BTN_X,
        BTN_Y,
        BTN_Z,
        BTN_L1,
        BTN_L2,
        BTN_L3,
        BTN_R1,
        BTN_R2,
        BTN_R3
    }

    // use the actual analog axis indices reported on certain controller (android)
    private final int DEF_X_AXIS_INDEX = 6;
    private final int DEF_Y_AXIS_INDEX = 7;


    // get the "virtual axis"
    float getAxisX(int axisIndex) {

        return analogAxes.x;
    }

    // get the "virtual axis"
    float getAxisY(int axisIndex) {

        return analogAxes.y;
    }

    /*
      axisIndex: index of changed axes (DEF AXIS for virtual axis from hatswitch)
      values[]: values of all 4 axes (only have all 4 if there are 2 analog mushrooms)
      Otherwise, virtual axes e.g. POV, WASD only have 2 axes.
                            -1.0
                       -1.0   +   +1.0
                            + 1.0
     */
    void setAxis(int axisIndex, float[] values) {
        /*
        multiple axes must be supported in order to have dpad (sometimes an axis)
        dpad if present reported on axis [6 7]
         */
        // wip whatever
        switch (GameWorld.getInstance().getControllerMode()) {
            default:
            case 0: // PS
            case 2: // PS (And)
                analogAxes.x = values[0];
                analogAxes.y = values[1];
                break;
            case 1: // XB (Win?)
                analogAxes.x = values[1];
                analogAxes.y = values[0];
                break;
        }
// pretty shaky assumption that these axes would only ever represent a d-pad
        if (DEF_X_AXIS_INDEX == axisIndex || DEF_Y_AXIS_INDEX == axisIndex) {
            analogAxes.x = values[DEF_X_AXIS_INDEX];
            analogAxes.y = values[DEF_Y_AXIS_INDEX];
        }
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


    private InputState evalNewInputState(boolean checkIsTouched) {

        InputState newInputState = incomingInputState;

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)
                || Gdx.input.isKeyPressed(Input.Keys.BACK)
                || getControlButton(VirtualButtons.BTN_START)
                ) {
            newInputState = InputState.INP_ESC;

        } else if (Gdx.input.isKeyPressed(Input.Keys.SPACE)
                || (Gdx.input.justTouched() && checkIsTouched)
                || getControlButton(VirtualButtons.BTN_A)
                ) {
            newInputState = InputState.INP_SELECT;

            pointer.set(Gdx.graphics.getHeight() / 2f, Gdx.graphics.getHeight() / 2f); // default to screen center or whatever

        } else if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                || getControlButton(VirtualButtons.BTN_B)
                ) {
            newInputState = InputState.INP_B2;
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.TAB)
                || getControlButton(VirtualButtons.BTN_SELECT)
                ) {
            newInputState = InputState.INP_START;
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

    void latchInputState() {
        nowInputState = getInputState();
    }

    void unlatchInputstate() {
        nowInputState = InputState.INP_NONE;
    }

    boolean isInputState(InputState inp) {
        return (nowInputState == inp);
    }

    /*
     * Eval and update the input state,
     * Return true if state is changes AND matches the wanted state. Else return false.
     * Only resets the Incoming State if changes AND matches.
     * So this is useful for doing a series of tests for different wanted states where it is OK
     * (and necessary) that the incoming state is not reset/cleared.
     */
    private boolean checkInputState(InputState wantedInputState) {

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


    private void setControlButton(int buttonIndex, boolean state) {

        if (buttonIndex < MAX_BUTTONS) {
            // lookup the virtual button id
            VirtualButtons bb = buttonmMapping[buttonIndex];

            if (null != bb) {
                buttonStates[bb.ordinal()] = state;
            }
        }
    }

    private boolean getControlButton(VirtualButtons wantedInputState) {

        int index = wantedInputState.ordinal();
        if (index > buttonStates.length) {
//    System.out.print("wtf");
            return false;
        } else
            return buttonStates[index];
    }

    public class DpadAxis {
        // Vector2 ??
        int x;
        int y;

        /* protect agains key held-over during screen transition ...my
         stupid wonky key handling  */
        boolean xBreak;
        boolean yBreak;

        void clear() {
            x = 0;
            y = 0;
        }

        public int getX() {

            int rt = 0;

            if (xBreak)
                rt = this.x;

            if (0 == this.x)
                xBreak = true;

            return rt;
        }

        public int getY() {

            int rt = 0;

            if (yBreak)
                rt = this.y;

            if (0 == this.y)
                yBreak = true;

            return rt;
        }

        public void setX(int x) {

            this.x = x;
        }

        public void setY(int y) {

            this.y = y;
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

    private DpadAxis dPadAxes = new DpadAxis(); // typically only 1 dPad, but it could be implemented as either an axis or 4 buttons while libGdx has it's own abstraction
    private AnalogAxis analogAxes = new AnalogAxis(); // would need array of max analog axes, for now just use one

    /*
     * "virtual dPad" provider using either controller POV or keyboard U/D/L/R
     *
     * NOTE: Android emulator: it gets keyboard input surprisingly on Windows (but not Linux it seems).
     * But glitchy and not worth considering.
     */
    /* Vector2 */
    DpadAxis getDpad(DpadAxis axisIndex) {

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
            dPadAxes.setY(-1); //            dPadAxes.y = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || PovDirection.south == povDir) {
            dPadAxes.setY(1); //            dPadAxes.y = 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || PovDirection.west == povDir) {
            dPadAxes.setX(-1); //            dPadAxes.x = -1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || PovDirection.east == povDir) {
            dPadAxes.setX(1); //            dPadAxes.x = 1;
        }

        // special sauce for PS/AND ctrl config
        int mode = GameWorld.getInstance().getControllerMode();
        switch (mode) {
            default:
            case 0: // PS
            case 1: // XB
            case 3: // PC
                break;
            case 2: // PS/AND
                dPadAxes.setX((int) analogAxes.x);
                dPadAxes.setY((int) analogAxes.y);
                break;
        }
        return dPadAxes;
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

        int mode = GameWorld.getInstance().getControllerMode();
        switch (mode) {
            default:
            case 0: // PS
            case 1: // XB
                buttonmMapping[0] = VirtualButtons.BTN_A;
                buttonmMapping[1] = VirtualButtons.BTN_B;
                buttonmMapping[6] = VirtualButtons.BTN_SELECT;
                buttonmMapping[7] = VirtualButtons.BTN_START;
                break;
            case 2: // PS/AND
                buttonmMapping[96] = VirtualButtons.BTN_A;
                buttonmMapping[97] = VirtualButtons.BTN_B;
                buttonmMapping[109] = VirtualButtons.BTN_SELECT;
                buttonmMapping[108] = VirtualButtons.BTN_START;
                break;
            case 3: // PCb
                buttonmMapping[0] = VirtualButtons.BTN_A;
                buttonmMapping[1] = VirtualButtons.BTN_B;
                buttonmMapping[8] = VirtualButtons.BTN_ESC; // how many "PC" game pads have a 3rd face-button?
                buttonmMapping[9] = VirtualButtons.BTN_SELECT;
                buttonmMapping[10] = VirtualButtons.BTN_START;
                break;
        }

        Controllers.addListener(new ControllerListenerAdapter() {

            private float[] axes = new float[MAX_AXES];

            int indexOf(Controller controller) {
                return Controllers.getControllers().indexOf(controller, true);
            }

            @Override
            public boolean buttonDown(Controller controller, int buttonIndex) {

                print("#" + indexOf(controller) + ", button " + buttonIndex + " down");

                setControlButton(buttonIndex, true);// buttons[buttonIndex] = true;
                return false;
            }

            @Override
            public boolean buttonUp(Controller controller, int buttonIndex) {

                print("#" + indexOf(controller) + ", button " + buttonIndex + " up");

                setControlButton(buttonIndex, false);//// buttons[buttonIndex] = false;
                return false;
            }

            /*
             * any axis moved then refresh the map i.e. read all conroller's axes
             */
            @Override
            public boolean axisMoved(Controller controller, int axisIndex, float value) {
                /*          -1.0
                       -1.0   +   +1.0  (0)
                            + 1.0        */
                for (int idx = 0; idx < MAX_AXES; idx++) {
                    axes[idx] = controller.getAxis(idx);
                }

                setAxis(axisIndex, axes);
                print("#" + indexOf(controller) + ", axes " + axisIndex + ": " + value);
//Gdx.app.log("axis moved", "axis index = " + axisIndex + " value = " + value);
                return false;
            }

            /*
             * pov moved may not be reported  ... it may report the hat-switch as axes (Android)
             */
            @Override
            public boolean povMoved(Controller controller, int povIndex, PovDirection value) {
                print("#" + indexOf(controller) + ", pov " + povIndex + ": " + value);

                Arrays.fill(axes, 0);
                int index = 0;

                if (value == PovDirection.west || value == PovDirection.southWest || value == PovDirection.northWest) {
                    index = DEF_X_AXIS_INDEX;
                    axes[index] = -1;
                }
                if (value == PovDirection.east || value == PovDirection.southEast || value == PovDirection.northEast) {
                    index = DEF_X_AXIS_INDEX;
                    axes[index] = +1;
                }
                if (value == PovDirection.north || value == PovDirection.northWest || value == PovDirection.northEast) {
                    index = DEF_Y_AXIS_INDEX;
                    axes[index] = -1;
                }
                if (value == PovDirection.south || value == PovDirection.southWest || value == PovDirection.southEast) {
                    index = DEF_Y_AXIS_INDEX;
                    axes[index] = +1;
                }

                setAxis(/*povIndex ... no virtualize it as axis */ index, axes);
                print("#" + indexOf(controller) + ", axes " + axes[0] + ": " + axes[1]);

                return false;
            }
        });
    }
}
