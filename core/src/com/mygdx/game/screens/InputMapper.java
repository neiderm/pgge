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
         1-           2-
      0- axes 0+   3- axes 3+
         1+           2+

      ESC=8 MOUSE=9 ENTER=10

-----------------------------

IPEGA PG-9076  "Microsoft X-Box 360 pad" (looks like a PS control)

  b4==L1      b5==R1
 Ax2==L2(+)  Ax5==R2(+)       Note on front anlg switches:  1==Down  -1==Up  (0 in the middle  ... wow)

                Y[3]
 6==Select   X[2]   B[1]
 7==Start       A[0]

  LR Axis0    Axis3
  UD Axis1    Axis4


-----------------------------

MYGT MY-C04  "MYGT Controller"  (looks like Xbox control)

   4==L1       5==R1
 Ax4==L2(+)  Ax4==R2(-)

                Y[3]
 6==Back     X[2]   B[1]
 7==Start       A[0]

 LR  Axis1    Axis3
 UD  Axis0    Axis2


-----------------------------
Android:
 L2->Ax5(+)  R2->Ax4(+)
 Dpad is Axis 6 (L-/R+) & 7 (U-/D+)
-----------------------------

*/

class InputMapper {

    static int NumberControlCfgTypes;
    private static final int MAX_AXES = 8;
    private static final int MAX_BUTTONS = 256; // arbitrary size to fit range of button index space

    // so this is the control switches abstrction
    public enum InputState {
        INP_NONE,
        INP_SELECT,
        INP_START,
        INP_A,
        INP_B;
    }

    private VirtualButtons[] buttonmMapping = new VirtualButtons[MAX_BUTTONS];
    private boolean[] buttonStates = new boolean[VirtualButtons.values().length];

    private Controller connectedCtrl;
    private Vector2 pointer = new Vector2();

    private InputState incomingInputState = InputState.INP_NONE;
    private InputState preInputState = InputState.INP_NONE;


    InputMapper() {

        initController();
        connectedCtrl = getConnectedCtrl(0);

        // bah ... debounce this (bounce from key on previous screen)
        checkInputState(InputState.INP_A); // seems to be necessary and effective with game pad ;)
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
    private static final int DEF_X_AXIS_INDEX = 6;
    private static final int DEF_Y_AXIS_INDEX = 7;

    @Deprecated
        // get the "virtual axis" hardcoded to axis 0
    float getAxisX(int notUsed) {
        return analogAxes[0];
    }
    @Deprecated
        // get the "virtual axis" hardcoded to axis 1
    float getAxisY(int notUsed) {
        return analogAxes[1];
    }

    // get the "virtual axis"
    float getAxis(int axisIndex) {

        return analogAxes[axisIndex];
    }

    // allows axes to be virtualized from external source, i.e keyboard
    void setAxis(int axisIndex, float axisValue){

        if (axisIndex < MAX_AXES){
            analogAxes[axisIndex] = axisValue;
        }
    }

    private void setAxes(float[] values) {

        analogAxes[0] = values[0];
        analogAxes[1] = values[1];

        for (int idx = 0; idx < MAX_AXES; idx++) {
            analogAxes[idx] = values[idx];
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
            newInputState = InputState.INP_START;

        } else if (Gdx.input.isKeyPressed(Input.Keys.SPACE)
                || (Gdx.input.justTouched() && checkIsTouched)
                || getControlButton(VirtualButtons.BTN_A)
                ) {
            newInputState = InputState.INP_A;

            pointer.set(Gdx.graphics.getHeight() / 2f, Gdx.graphics.getHeight() / 2f); // default to screen center or whatever

        } else if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                || getControlButton(VirtualButtons.BTN_B)
                ) {
            newInputState = InputState.INP_B;
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.TAB)
                || getControlButton(VirtualButtons.BTN_SELECT)
                ) {
            newInputState = InputState.INP_SELECT;
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

        setInputState(InputState.INP_A);
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


    private DpadAxis dPadAxes = new DpadAxis(); // typically only 1 dPad, but it could be implemented as either an axis or 4 buttons while libGdx has it's own abstraction
    private float[] analogAxes = new float[MAX_AXES];

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
            dPadAxes.setY(-1);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || PovDirection.south == povDir) {
            dPadAxes.setY(1);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || PovDirection.west == povDir) {
            dPadAxes.setX(-1);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || PovDirection.east == povDir) {
            dPadAxes.setX(1);
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
                dPadAxes.setX( (int)analogAxes[0]);
                dPadAxes.setY( (int)analogAxes[1]);
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

                setControlButton(buttonIndex, true);
                return false;
            }

            @Override
            public boolean buttonUp(Controller controller, int buttonIndex) {

                print("#" + indexOf(controller) + ", button " + buttonIndex + " up");

                setControlButton(buttonIndex, false);
                return false;
            }

            /*
             * any axis moved then refresh the map i.e. read all conroller's axes
             */
            @Override
            public boolean axisMoved(Controller controller, int axisIndex, float value) {

                for (int idx = 0; idx < MAX_AXES; idx++) {
                    axes[idx] = controller.getAxis(idx);
                }

                float tmp;

                switch (GameWorld.getInstance().getControllerMode()) {
                    default:
                    case 0: // PS
                    case 2: // PS (And) ... Dpad is axis
                        // bah crap have to do this here ?
                        if (DEF_X_AXIS_INDEX == axisIndex || DEF_Y_AXIS_INDEX == axisIndex){
                            axes[0] = axes[DEF_X_AXIS_INDEX];
                            axes[1] = axes[DEF_Y_AXIS_INDEX];
                        }
                        break;
                    case 1: // XB (Win?)
                        tmp = axes[0];
                        axes[0] = axes[1];
                        axes[1] = tmp;
                        break;
                }

                setAxes(axes);
                print("#" + indexOf(controller) + ", axes " + axisIndex + ": " + value);
                return false;
            }

            /*
             * pov moved may not be reported  ... it may report the dpad as axes (Android)
             * so virtualize it as an axis regardless
             */
            @Override
            public boolean povMoved(Controller controller, int povIndex, PovDirection value) {
                print("#" + indexOf(controller) + ", pov " + povIndex + ": " + value);

                Arrays.fill(axes, 0); // set all 0, then update 1 or 2 axes depending on POV direction

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

                setAxes(axes);

                return false;
            }
        });
    }
}
