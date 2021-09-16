/*
 * Copyright (c) 2021 Glenn Neidermeier
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
Tested Controllers:

-----------------------------
Belkin Nostromo n45
"Nostromo Nostromo n45 Dual Analog Gamepad" (Linux, IOS)
"Nostromo n45 Dual Analog Gamepad" (Win 10?)
 note: dPad reported as POV (WIN10/Linux)
       dPad not reported on MacBook

       [5]                       [7]
       [4]                       [6]

       [U]
   [L]  # [R]                   B4[3]
      [D]     [8][9][10]   B3[2]  #  B2[1]
                                B1[0]
         1-          2-
      0- #  0+   3-  #  3+
         1+          2+

      ESC=8 MOUSE=9 ENTER=10

-----------------------------
IPEGA PG-9076
"Xbox 360 Controller for Windows" (connected USB Win10)
"Microsoft X-Box 360 pad"   (connected USB Linux)
"PG-9076"  (connected B/T Android)
 note: dPad reported as POV (Win/Linux)
 note: dPad reported as axes 6+7 (Android)

  b4==L1      b5==R1
 Ax2==L2(+)  Ax5==R2(+)       Note on front anlg switches:  1==Down  -1==Up  (0 in the middle  ... wow)

                 Y[3]
 6==Select   X[2] #  B[1]
 7==Start        A[0]

  LR Axis0    Axis3
  UD Axis1    Axis4

-----------------------------
MYGT MY-C04
"Xbox 360 Controller for Windows" (Win10)
"MYGT Controller" (Android)
 note: 2.4 Gz interface, USB port is charge-only
 note: dPad reported as POV (Win10)

   4==L1       5==R1
 Ax4==L2(+)  Ax4==R2(-)

                 Y[3]
 6==Back     X[2] #  B[1]
 7==Start        A[0]

 LR  Axis1    Axis3
 UD  Axis0    Axis2

-----------------------------
Sony Wireless Controller
"PLAYSTATION(R)3 Controller" (connected by USB WIN10)
"Sony PLAYSTATION(R)3 Controller" (connected by USB IOS, Linux)

   9==L1      10==R1
 Ax4==L2     Ax5==R2

 4==SELECT
 5==PS
 6==START

 LR  BT13/BT14
 LR  BT11/BT12

-----------------------------
Android:
 L2->Ax5(+)  R2->Ax4(+)
 Dpad is Axis 6 (L-/R+) & 7 (U-/D+)
-----------------------------
*/

public class InputMapper {

    static int numberControlCfgTypes;

    private static final int MAX_AXES = 8;
    private static final int MAX_BUTTONS = 256; // arbitrary size to fit range of button index space
    // virtual axis assignments, probably an enum would be better
    public static final int VIRTUAL_AD_AXIS = 0; // WASD "X" axis
    public static final int VIRTUAL_WS_AXIS = 1; // WASD "Y" axis
    public static final int VIRTUAL_X1_AXIS = 2; // right anlg stick "X" (if used)
    public static final int VIRTUAL_Y1_AXIS = 3; // right anlg stick "Y" (if used)
    public static final int VIRTUAL_L2_AXIS = 4; // front button "left 2" (if used)
    public static final int VIRTUAL_R2_AXIS = 5; // front button "right 2" (if used)

    static final int VIRTUAL_AXES_SZ = 6;

//    public enum VirtualAxes {
//        VIRTUAL_AD_AXIS, // WASD "X" axis
//        VIRTUAL_WS_AXIS, // WASD "Y" axis
//        VIRTUAL_X1_AXIS, // right anlg stick "X" (if used)
//        VIRTUAL_Y1_AXIS, // right anlg stick "Y" (if used)
//        VIRTUAL_L2_AXIS, // front button "left 2" (if used)
//        VIRTUAL_R2_AXIS // front button "right 2" (if used)
//    }

    /*
     * enumerate the various input events with which the screens may (poll/notify?)
     */
    public enum InputState {
        INP_NONE,
        INP_VIEW,
        INP_MENU,
        INP_FIRE1,   // A
        INP_FIRE2,   // B
        INP_BROVER,  // Y (brake/roll-over)
        INP_FUNC,    // X (tbd)
        INP_SEL1,    // L1
        INP_SEL2     // R1
    }

    private final VirtualButtons[] buttonmMapping = new VirtualButtons[MAX_BUTTONS];
    private final int[] buttonStateDebCts = new int[VirtualButtons.values().length];
    private final boolean[] buttonStates = new boolean[VirtualButtons.values().length];
    private final Vector2 pointer = new Vector2();

    private Controller connectedCtrl;
    private InputState incomingInputState = InputState.INP_NONE;
    private InputState preInputState = InputState.INP_NONE;

    /*
     * abstraction of controller buttons
     */
    public enum VirtualButtons {
        BTN_NONE,
        BTN_ESC, // n45 'ESC'
        BTN_SELECT, // n45 'MOUSE'
        BTN_START, // n45 'ENTER'
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

    InputMapper() {
        initController();
        connectedCtrl = getConnectedCtrl(0);
        /*
         * Make sure to test that the INP FIRE1 (Space key or X/A button) can be held down and does
         * not repeat going from one screen to the next.
         * Leaving incomingInputState not explicitly initialized doesn't seem to have any ill effects.
         */
        preInputState = InputState.INP_FIRE1;
    }

    // get the "virtual axis"
    float getAxis(int axisIndex) {
        return analogAxes[axisIndex];
    }

    // allows axes to be virtualized from external source, i.e keyboard
    void setAxis(int axisIndex, float axisValue) {
        if (axisIndex < MAX_AXES) {
            analogAxes[axisIndex] = axisValue;
        }
    }

    private static Controller getConnectedCtrl(int selectControl) {
        // If a controller is connected, find it and grab a link to it
        Controller connectedCtrl = null;
        int i = 0;
        for (Controller controller : Controllers.getControllers()) {
            Gdx.app.log(
                    "InputMapper", "using connected controller " + controller.getName());
            if (i++ == selectControl) {
                connectedCtrl = controller;
                break;
            }
        }
        return connectedCtrl;
    }

    /*
     * Available physical devices - which can include e.g. gamepad controller buttons, keyboard
     * input, as well as virtual buttons on the touch screen - are multiplexed into the various
     * discrete input abstractions.
     * todo: how Input.Keys.BACK generated in Android Q
     * todo: table
     *          Keyboard   Android TS       Controller
     * ESCAPE   Esc        Input.Keys.BACK  Start Button
     */
    private InputState evalNewInputState(boolean checkIsTouched) {

        InputState newInputState = incomingInputState;

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)
                || Gdx.input.isKeyPressed(Input.Keys.BACK)
                || getControlButton(VirtualButtons.BTN_START)) {
            newInputState = InputState.INP_MENU;
        } else if (Gdx.input.isKeyPressed(Input.Keys.TAB)
                || getControlButton(VirtualButtons.BTN_SELECT)) {
            newInputState = InputState.INP_VIEW;
        } else if (Gdx.input.isKeyPressed(Input.Keys.SPACE)
                || (Gdx.input.justTouched() && checkIsTouched)
                || getControlButton(VirtualButtons.BTN_A)) {
            newInputState = InputState.INP_FIRE1;
            // default to screen center just because
            pointer.set(Gdx.graphics.getHeight() / 2f, Gdx.graphics.getHeight() / 2f);
        } else if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                || getControlButton(VirtualButtons.BTN_B)) {
            newInputState = InputState.INP_FIRE2;
        } else if (Gdx.input.isKeyPressed(Input.Keys.ENTER)
                || getControlButton(VirtualButtons.BTN_Y)) {
            newInputState = InputState.INP_BROVER;
        } else if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                || getControlButton(VirtualButtons.BTN_L1)) {
            newInputState = InputState.INP_SEL1;
        } else if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)
                || getControlButton(VirtualButtons.BTN_R1)) {
            newInputState = InputState.INP_SEL2;
        }
        return newInputState;
    }

    /**
     * Evaluate discrete inputs and return the enum id of the active input if any.
     * If incoming input state has changed from previous value, then update with stored
     * input state and return it. If no change, returns NONE.
     * This api is necessary because checkInputState() de-latches (resets) the global Input State
     * so can't be used successively to check for more than one state  .... bah
     *
     * @param checkIsTouched if touch screen input should be included in input mux
     * @return enum constant of the currently active input if any or INP_NONE
     */
    InputState getInputState(boolean checkIsTouched) {

        InputState newInputState = evalNewInputState(checkIsTouched);
        InputState debouncedInputState = InputState.INP_NONE;

        if (preInputState != newInputState) { // debounce
            debouncedInputState = newInputState;
        }
        preInputState = newInputState;
        incomingInputState = InputState.INP_NONE; // unlatch the input state
        return debouncedInputState;
    }

    InputState getInputState() {
        return getInputState(false);
    }

    private InputState nowInputState;

    void latchInputState() {
        nowInputState = getInputState();
    }

    boolean isInputState(InputState inp) {
        return (nowInputState == inp);
    }

    void setControlButton(int buttonIndex, boolean state) {
        if (buttonIndex < MAX_BUTTONS) {
            // lookup the virtual button id
            VirtualButtons bb = buttonmMapping[buttonIndex];
            if (null != bb) {
                buttonStates[bb.ordinal()] = state;

                switch (GameWorld.getInstance().getControllerMode()) {
                    default:
                    case 0: // Linux USB (PG9076)
                    case 1: // Windows USB (2.4G ?)
                    case 2: // Android B/T
                        break;
                    case 3: // PC USB (N45)
                        // L2/R2 show up as buttons - virtualize as 2 independent axes ranging [0:+1]
                        int val = state ? 1 : 0;
                        if (VirtualButtons.BTN_L2 == bb) {
                            analogAxes[VIRTUAL_L2_AXIS] = val;
                        }
                        if (VirtualButtons.BTN_R2 == bb) {
                            analogAxes[VIRTUAL_R2_AXIS] = val;
                        }
                        break;
                }
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

    boolean getDebouncedContrlButton(VirtualButtons vbutton) {
        return getDebouncedContrlButton(vbutton, false, 5);
    }

    boolean getDebouncedContrlButton(VirtualButtons vbutton, int repeatPeriod) {
        return getDebouncedContrlButton(vbutton, true, repeatPeriod);
    }

    private boolean getDebouncedContrlButton(VirtualButtons vbutton, boolean letRepeat, int repeatPeriod) {

        int switchIndex = vbutton.ordinal();
        boolean rv = false;

        if (getControlButton(vbutton) && (0 == buttonStateDebCts[switchIndex])) {
            rv = true;
            // controller may emit several down/up events on a "single" button press/release
            buttonStateDebCts[switchIndex] = repeatPeriod;
        }
        // if user has let go of button, then reduce the countdown to the debounce time
        if (!getControlButton(vbutton)) {
            final int Debounce_Time = 15;
            if (buttonStateDebCts[switchIndex] > Debounce_Time) {
                buttonStateDebCts[switchIndex] = Debounce_Time;
            }
        }
        if (!getControlButton(vbutton) || letRepeat) {
            buttonStateDebCts[switchIndex] -= 2;
            if (buttonStateDebCts[switchIndex] < 0) {
                buttonStateDebCts[switchIndex] = 0;
            }
        }
        return rv;
    }

    public static class DpadAxis {
        // Vector2 ??
        int x;
        int y;
        /* protect against key held over during screen transition ... funky key handling ;) */
        boolean xBreak;
        boolean yBreak;

        void clear() {
            x = 0;
            y = 0;
        }

        public int getX() {
            int rt = 0;

            if (xBreak) {
                rt = this.x;
            }
            if (0 == this.x) {
                xBreak = true;
            }
            return rt;
        }

        public int getY() {
            int rt = 0;

            if (yBreak) {
                rt = this.y;
            }
            if (0 == this.y) {
                yBreak = true;
            }
            return rt;
        }

        public void setX(int x) {
            this.x = x;
        }

        public void setY(int y) {
            this.y = y;
        }
    }

    // dPad: POV, axis, or 4 buttons?
    private final DpadAxis dPadAxes = new DpadAxis();
    private final float[] analogAxes = new float[MAX_AXES];

    /*
     * "virtual dPad" provider using either controller POV or keyboard U/D/L/R
     * note: POV to be eliminated with libGdx Controllers 2.0
     *
     * NOTE: observed that Android emulator reports keyboard input (Windows host) but seems unreliable.
     */
    DpadAxis getDpad() {
        dPadAxes.clear();

        PovDirection povDir = PovDirection.center;
        if (null != connectedCtrl) {
            povDir = connectedCtrl.getPov(0);
        }

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

        // handle exceptions to the general rules above
        int mode = GameWorld.getInstance().getControllerMode();
        switch (mode) {
            default:
            case 0: // Ipega PG-9076 Linux USB
            case 1: // Windows USB B/T
            case 3: // Belkin n45 Linux USB
                break;
            case 2: // Android
                // map dpad to virtual axes
                dPadAxes.setX((int) analogAxes[VIRTUAL_AD_AXIS]); // DPAD_X_AXIS
                dPadAxes.setY((int) analogAxes[VIRTUAL_WS_AXIS]); // DPAD_Y_AXIS
                break;
        }
        return dPadAxes;
    }

    private void print(String message) {
        Gdx.app.log("InputMapper", message);
    }

    /*
     * enumerates connected controllers (todo: list known controllers in a table)
     * sets button mapping
     */
    private void initController() {
        // print the currently connected controllers to the console
        print("initController(): available controllers: " + Controllers.getControllers().size);
        int i = 0;
        for (Controller controller : Controllers.getControllers()) {
            print("#" + i++ + ": " + controller.getName());
        }
        // controller identification is not consistent between platforms
        switch (GameWorld.getInstance().getControllerMode()) {
            default:
            case 0: // Ipega PG-9076 Linux USB
            case 1: // Windows (USB, B/T)
                buttonmMapping[0] = VirtualButtons.BTN_A;
                buttonmMapping[1] = VirtualButtons.BTN_B;
                buttonmMapping[2] = VirtualButtons.BTN_X;
                buttonmMapping[3] = VirtualButtons.BTN_Y;
                buttonmMapping[4] = VirtualButtons.BTN_L1;
                buttonmMapping[5] = VirtualButtons.BTN_R1;
                buttonmMapping[6] = VirtualButtons.BTN_SELECT;
                buttonmMapping[7] = VirtualButtons.BTN_START;
                // Turbo?
                break;
            case 2: // Android
                buttonmMapping[96] = VirtualButtons.BTN_A;
                buttonmMapping[97] = VirtualButtons.BTN_B;
                buttonmMapping[99] = VirtualButtons.BTN_X;
                buttonmMapping[100] = VirtualButtons.BTN_Y;
                buttonmMapping[102] = VirtualButtons.BTN_L1;
                buttonmMapping[103] = VirtualButtons.BTN_R1;
                buttonmMapping[109] = VirtualButtons.BTN_SELECT;
                buttonmMapping[108] = VirtualButtons.BTN_START;
                break;
            case 3: // Belkin n45 Linux USB
                buttonmMapping[0] = VirtualButtons.BTN_A; // B1
                buttonmMapping[1] = VirtualButtons.BTN_B; // B2
                buttonmMapping[2] = VirtualButtons.BTN_X; // B3
                buttonmMapping[3] = VirtualButtons.BTN_Y; // B4
                buttonmMapping[4] = VirtualButtons.BTN_L1; // T3
                buttonmMapping[6] = VirtualButtons.BTN_R1; // T1
                buttonmMapping[5] = VirtualButtons.BTN_L2; // T4  (virtualize as L2/R2 axis)
                buttonmMapping[7] = VirtualButtons.BTN_R2; // T2  (virtualize as L2/R2 axis)
                buttonmMapping[8] = VirtualButtons.BTN_ESC;    // 3rd function button
                buttonmMapping[9] = VirtualButtons.BTN_SELECT; // MOUSE
                buttonmMapping[10] = VirtualButtons.BTN_START; // ENTER
                break;
        }

        Controllers.addListener(new ControllerListenerAdapter() {

            private final float[] rawAxes = new float[MAX_AXES];
            private final float[] remappedAxes = new float[MAX_AXES];

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

            /**
             * Event handler for movement of controller axes
             * @param controller controller
             * @param axisIndex axis index
             * @param value axis value
             * @return false indicating that the event has been handled
             */
            @Override
            public boolean axisMoved(Controller controller, int axisIndex, float value) {
                /*
                the naive approach taken here is (assuming first that axes mapping are one-size-fits-all)
                is to read all axes from the controller (not just the indicated axisIndex) and then
                adjust as necessary for the specific platform/device combination.
                 */
                for (int idx = 0; idx < MAX_AXES; idx++) {
                    rawAxes[idx] = controller.getAxis(idx);
                    remappedAxes[idx] = rawAxes[idx];
                }

                // android dpad analog axes
                final int DPAD_X_AXIS = 6;
                final int DPAD_Y_AXIS = 7;

                switch (GameWorld.getInstance().getControllerMode()) {
                    default:
                        // Linux USB (Ipega PG-9076)
                    case 0:
                        // L2/R2 are analog (positive-range only)
                        remappedAxes[VIRTUAL_L2_AXIS] = rawAxes[2];
                        remappedAxes[VIRTUAL_R2_AXIS] = rawAxes[5];
                        // set the X1 and Y1 axes
                        remappedAxes[VIRTUAL_X1_AXIS] = rawAxes[3];
                        remappedAxes[VIRTUAL_Y1_AXIS] = rawAxes[4];
                        break;
                    // Windows (USB, B/T)
                    case 1:
                        // swap the WS and AD axes
                        remappedAxes[VIRTUAL_AD_AXIS] = rawAxes[1];
                        remappedAxes[VIRTUAL_WS_AXIS] = rawAxes[0];
                        // swap the X1 and Y1 axes
                        remappedAxes[VIRTUAL_X1_AXIS] = rawAxes[3];
                        remappedAxes[VIRTUAL_Y1_AXIS] = rawAxes[2];
                        break;
                    // Android (B/T)
                    case 2:
                        // Android reports Dpad as axes - set it only if it was actually moved?
                        if ((DPAD_X_AXIS == axisIndex) || (DPAD_Y_AXIS == axisIndex)) {
                            remappedAxes[VIRTUAL_AD_AXIS] = rawAxes[DPAD_X_AXIS];
                            remappedAxes[VIRTUAL_WS_AXIS] = rawAxes[DPAD_Y_AXIS];
                        }
                        // L2/R2 are analog axes range [0:1.0]
                        remappedAxes[VIRTUAL_L2_AXIS] = rawAxes[5];
                        remappedAxes[VIRTUAL_R2_AXIS] = rawAxes[4];
                        break;
                    // Linux USB (BELKIN NOSTROMO n45)
                    case 3:
                        // swap the X1 and Y1 axes  todo: Windos but not Linux?
                        remappedAxes[VIRTUAL_X1_AXIS] = rawAxes[3];
                        remappedAxes[VIRTUAL_Y1_AXIS] = rawAxes[2];
                        break;
                }
                System.arraycopy(remappedAxes, 0, analogAxes, 0, MAX_AXES);
                print("#" + indexOf(controller) + ", rawAxes " + axisIndex + ": " + value);
                return false;
            }

            /*
             * Virtualize POV (dpad) as an axis.
             * POV may not be reported on all platform, e.g. Android reports the dpad as axes.
             * todo POV to be removed with libGDX Controllers > 2.0
             */
            @Override
            public boolean povMoved(Controller controller, int povIndex, PovDirection value) {
                print("#" + indexOf(controller) + ", POV " + povIndex + ": " + value);

                Arrays.fill(rawAxes, 0); // set all 0, then update 1 or 2 axes depending on POV direction

                if (value == PovDirection.west || value == PovDirection.southWest || value == PovDirection.northWest) {
                    rawAxes[0] = -1;
                }
                if (value == PovDirection.east || value == PovDirection.southEast || value == PovDirection.northEast) {
                    rawAxes[0] = +1;
                }
                if (value == PovDirection.north || value == PovDirection.northWest || value == PovDirection.northEast) {
                    rawAxes[1] = -1;
                }
                if (value == PovDirection.south || value == PovDirection.southWest || value == PovDirection.southEast) {
                    rawAxes[1] = +1;
                }
                System.arraycopy(rawAxes, 0, analogAxes, 0, MAX_AXES);
                return false;
            }
        });
    }
}
