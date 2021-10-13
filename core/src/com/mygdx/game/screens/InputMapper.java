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
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
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

    // this is set going into Game Pad Config ... not sure intended purpose
    @SuppressWarnings("unused")
    private static int numberControlCfgTypes;

    static void incrementNumberControlCfgTypes() {
        numberControlCfgTypes += 1;
    }

    // virtual axis assignments, probably an enum would be better
    public static final int VIRTUAL_AD_AXIS = 0; // WASD "X" axis
    public static final int VIRTUAL_WS_AXIS = 1; // WASD "Y" axis
    public static final int VIRTUAL_X1_AXIS = 2; // right anlg stick "X" (if used)
    public static final int VIRTUAL_Y1_AXIS = 3; // right anlg stick "Y" (if used)
    public static final int VIRTUAL_L2_AXIS = 4; // front button "left 2" (if used)
    public static final int VIRTUAL_R2_AXIS = 5; // front button "right 2" (if used)

    /*
     * abstraction of controller buttons
     */
    public enum VirtualButtonCode {
        BTN_NONE,
        BTN_ESC, // n45 'ESC' (unique to this device and not used)
        BTN_SELECT, // n45 'MOUSE' (View)
        BTN_START, // n45 'ENTER' (Menu/Esc)
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
        BTN_R3,
        BTN_UP,
        BTN_DOWN,
        BTN_LEFT,
        BTN_RIGHT
    }

    /**
     * Support of any known controller button-mapping with values in range 0:255.
     * The array is sparsely populated but at the cost of a little unused and inefficiently
     * allocated memory e.g.:
     * virtualButtonCodes[96] = VirtualButtons.BTN_A;
     * <p>
     * In the Key Up and Key Down event handlers, the virtual button code can be determined from the
     * incoming control button code with minimal effort - first the incoming controller button code
     * is used as an index to retrieve the corresponding virtual button code from the virtual button
     * code array, then finally the new button state is stored using the virtual button enum ordinal
     * as the index into the virtual button states array:
     * <p>
     * VirtualButtonCode vbCode = virtualButtonCodes[ controllerButtonCode ];
     * assert (null != vbCode)
     * virtualButtonStates[ vbCode.ordinal() ] = newButtonState;
     */
    private static final int MAX_BUTTONS = 256; // arbitrary size to fit range of button index space

    private final VirtualButtonCode[] virtualButtonCodes = new VirtualButtonCode[MAX_BUTTONS];

    private final boolean[] virtualButtonStates = new boolean[VirtualButtonCode.values().length];

    private final int[] virtualButtonDebounceCounts = new int[VirtualButtonCode.values().length];

    private static final int MAX_AXES = 8;

    private final float[] analogAxes = new float[MAX_AXES];

    @SuppressWarnings("unused") // e.g. connectedCtrl.getPov(0);
    private Controller connectedCtrl;

    InputMapper() {
        // connected controller .. not used for much anything right now
        final int CONTROLLER_ZERO = 0; // presently only 1 player is supported
        connectedCtrl = getConnectedCtrl(CONTROLLER_ZERO);

        setControllerButtonMapping();
    }

    /**
     * use axix like virtual buttons (e.g. UI menus, checklists etc.)
     *
     * @param axisIndex id of axis to read e.g. InputMapper.VIRTUAL_WS_AXIS etc.
     * @return integer axis value {-1, 0, 1}
     */
    int getAxisI(int axisIndex) {

        float axisX = getAxis(axisIndex);
        int step = 0;
        if (axisX > 0.8f) {
            step = 1;
        } else if (axisX < -0.8f) {
            step = -1;
        }
        return step;
    }

    /**
     * use axis as proportional controller (model control)
     *
     * @param axisIndex id of axis to read e.g. InputMapper.VIRTUAL_WS_AXIS etc.
     * @return float axis value [-1::1]
     */
    private float getAxis(int axisIndex) {
        return analogAxes[axisIndex];
    }

    // allows axes to be virtualized from external source, i.e keyboard WASD, controller, touchscreen
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

    /**
     * Method for Controller Listener button up/down event and or for Input Listener key up/down event.
     * The incoming button code is used as index to find the corresponding virtual button definition.
     *
     * @param buttonIndex    the button code
     * @param newButtonState incoming button state
     */
    private void setControlButton(int buttonIndex, boolean newButtonState) {
        if (buttonIndex < MAX_BUTTONS) {
            // lookup the virtual button code
            VirtualButtonCode vbCode = virtualButtonCodes[buttonIndex];
            setControlButton(vbCode, newButtonState);
        }
    }

    /**
     * This one is public as it is preferable to use the virtual button code
     *
     * @param vbCode         vbutton Virtual Button Code of tested button
     * @param newButtonState new button state to set
     */
    void setControlButton(VirtualButtonCode vbCode, boolean newButtonState) {
        if (null != vbCode) {
            virtualButtonStates[vbCode.ordinal()] = newButtonState;

            // left/right arrows (Select Screen)
            if (VirtualButtonCode.BTN_LEFT == vbCode) {
                analogAxes[VIRTUAL_AD_AXIS] = newButtonState ? (-1) : 0;
            }
            if (VirtualButtonCode.BTN_RIGHT == vbCode) {
                analogAxes[VIRTUAL_AD_AXIS] = newButtonState ? 1 : 0;
            }

            switch (GameWorld.getInstance().getControllerMode()) {
                default:
                case 0: // Linux USB (PG9076)
                    // WASD==11,12,13,14
                case 1: // Windows USB (2.4G ?)
                case 2: // Android B/T
                    // PG9076 19,20,21,22
                    break;
                case 3: // PC USB (N45)
                    // WASD==11,12,13,14
                    // L2/R2 show up as buttons - virtualize as 2 independent axes ranging [0:+1]
                    int val = newButtonState ? 1 : 0;
                    if (VirtualButtonCode.BTN_L2 == vbCode) {
                        analogAxes[VIRTUAL_L2_AXIS] = val;
                    }
                    if (VirtualButtonCode.BTN_R2 == vbCode) {
                        analogAxes[VIRTUAL_R2_AXIS] = val;
                    }
                    break;
            }
        }
    }

    /**
     * get control button - no debounce
     *
     * @param vbutton Virtual Button Code of tested button
     * @return state of wanted input
     */
    boolean getControlButton(VirtualButtonCode vbutton) {
        int index = vbutton.ordinal();
        if (index > virtualButtonStates.length) {
            return false;
        } else {
            return virtualButtonStates[index];
        }
    }

    /**
     * * get control button with configurable debounce time or repeat period
     *
     * @param vbutton      Virtual Button Code of tested button
     * @param letRepeat    boolean whether to let button repeat or not
     * @param repeatPeriod period of repetition if held down, otherwise sets the debounce time if letRepeat==false
     * @return state of tested button
     */
    boolean getControlButton(VirtualButtonCode vbutton, boolean letRepeat, int repeatPeriod) {

        int switchIndex = vbutton.ordinal();
        boolean rv = false;

        if (getControlButton(vbutton)) {
            if (0 == virtualButtonDebounceCounts[switchIndex]) {
                rv = true;
                // controller may emit several down/up events on a "single" button press/release
                virtualButtonDebounceCounts[switchIndex] = repeatPeriod;
//                setControlButton(vbutton, false); // maybe
            }
        } else { // if (!getControlButton(vbutton)) {
            // if user has let go of button, then reduce the countdown to the debounce time
            final int Debounce_Time = 15;
            if (virtualButtonDebounceCounts[switchIndex] > Debounce_Time) {
                virtualButtonDebounceCounts[switchIndex] = Debounce_Time;
            }
            if (letRepeat) {
                virtualButtonDebounceCounts[switchIndex] -= 2;
                if (virtualButtonDebounceCounts[switchIndex] < 0) {
                    virtualButtonDebounceCounts[switchIndex] = 0;
                }
            }
        }
        return rv;
    }

    /**
     * class is static and Controller Adapter holds an instance of it
     */
    static class CtrlButton {
        VirtualButtonCode vbcode;
        boolean state; // open/up closed/down
        boolean isRepeated; // true = repeated, false = debounced and not repeated
        int timeout; // if is repeated, then repeat interval, otherwise debounce timeout

        CtrlButton() {
            this.state = false; // open/up closed/down
            this.isRepeated = false; // true = repeated, false = debounced and not repeated
            this.timeout = 0; // if is repeated, then repeat interval, otherwise debounce timeout
            this.vbcode = VirtualButtonCode.BTN_NONE;
        }

        CtrlButton(VirtualButtonCode vbcode) {
            this(vbcode, false, 0);
        }

        CtrlButton(VirtualButtonCode vbcode, boolean isRepeated, int timeout) {
            this.vbcode = vbcode;
            this.isRepeated = isRepeated;
            this.timeout = timeout;
        }
    }

    public static class ControlBundle {

        private CtrlButton[] cbuttons = new CtrlButton[MAX_BUTTONS];
        private final float[] analogAxes = new float[MAX_AXES];

        public ControlBundle() {
            setButtons(new CtrlButton(InputMapper.VirtualButtonCode.BTN_A), new CtrlButton());
        }

        void setButtons(CtrlButton... cbuttons) {
            if (cbuttons.length < MAX_BUTTONS) {
                System.arraycopy(cbuttons, 0, this.cbuttons, 0, cbuttons.length);
            }
        }

        CtrlButton getButton(int index) {
            if (index < cbuttons.length) {
                return cbuttons[index];
            }
            return null;
        }

        public void setCbuttonState(int index, boolean state) {
            if (null != cbuttons[index]) { /* && (index < MAX_BUTTONS) */
                cbuttons[index].state = state;
            }
        }

        public boolean getCbuttonState(int index) {
            if (index < cbuttons.length) {
                return cbuttons[index].state;
            }
            return false;
        }

        public void setAxis(int index, float value) {
            if (index < MAX_AXES) {
                this.analogAxes[index] = value;
            }
        }

        public float getAxis(int index) {
            if (index < MAX_AXES) {
                return this.analogAxes[index];
            }
            return 0;
        }
    }

    private void sampleAxis(int index, ControlBundle cbundle) {
        cbundle.setAxis(index, getAxis(index));
    }

    /**
     * using the C Bundle helps reduce uses of the InputMapper.VIRTUAL_XXXX
     *
     * @param cbundle Control Bundle instance passed from owning class
     */
    void updateControlBundle(ControlBundle cbundle) {

        sampleAxis(InputMapper.VIRTUAL_WS_AXIS, cbundle);
        sampleAxis(InputMapper.VIRTUAL_AD_AXIS, cbundle);
        sampleAxis(InputMapper.VIRTUAL_X1_AXIS, cbundle);
        sampleAxis(InputMapper.VIRTUAL_Y1_AXIS, cbundle);
        sampleAxis(InputMapper.VIRTUAL_L2_AXIS, cbundle);
        sampleAxis(InputMapper.VIRTUAL_R2_AXIS, cbundle);

        CtrlButton cbutton;
        // todo loopit
        cbutton = cbundle.getButton(0); // todo: 0
        if (null != cbutton) {
            boolean state = getControlButton(
                    InputMapper.VirtualButtonCode.BTN_A, cbutton.isRepeated, cbutton.timeout);
            cbundle.setCbuttonState(0, state);
        }
        cbutton = cbundle.getButton(1); // todo: 1
        if (null != cbutton) {
            boolean state = getControlButton(InputMapper.VirtualButtonCode.BTN_B);
            cbundle.setCbuttonState(1, state);
        }
    }

    private void print(String message) {
        Gdx.app.log("InputMapper", message);
    }

    /*
     * Enumerates connected controllers and configures virtual button mapping.
     */
    private void setControllerButtonMapping() {
        // print the currently connected controllers to the console
        print("available controllers: " + Controllers.getControllers().size);
        int i = 0;
        for (Controller controller : Controllers.getControllers()) {
            print("#" + i++ + ": " + controller.getName());
        }
        // controller identification is not consistent between platforms
        switch (GameWorld.getInstance().getControllerMode()) {
            default:
            case 0: // Ipega PG-9076 Linux USB
            case 1: // Windows (USB, B/T)
                virtualButtonCodes[0] = VirtualButtonCode.BTN_A;
                virtualButtonCodes[1] = VirtualButtonCode.BTN_B;
                virtualButtonCodes[2] = VirtualButtonCode.BTN_X;
                virtualButtonCodes[3] = VirtualButtonCode.BTN_Y;
                virtualButtonCodes[4] = VirtualButtonCode.BTN_L1;
                virtualButtonCodes[5] = VirtualButtonCode.BTN_R1;
                virtualButtonCodes[6] = VirtualButtonCode.BTN_SELECT;
                virtualButtonCodes[7] = VirtualButtonCode.BTN_START;
                // Turbo?
                break;
            case 2: // Android
                virtualButtonCodes[96] = VirtualButtonCode.BTN_A;
                virtualButtonCodes[97] = VirtualButtonCode.BTN_B;
                virtualButtonCodes[99] = VirtualButtonCode.BTN_X;
                virtualButtonCodes[100] = VirtualButtonCode.BTN_Y;
                virtualButtonCodes[102] = VirtualButtonCode.BTN_L1;
                virtualButtonCodes[103] = VirtualButtonCode.BTN_R1;
                virtualButtonCodes[109] = VirtualButtonCode.BTN_SELECT;
                virtualButtonCodes[108] = VirtualButtonCode.BTN_START;
                break;
            case 3: // Belkin n45 Linux/Windows USB
                virtualButtonCodes[0] = VirtualButtonCode.BTN_A; // B1
                virtualButtonCodes[1] = VirtualButtonCode.BTN_B; // B2
                virtualButtonCodes[2] = VirtualButtonCode.BTN_X; // B3
                virtualButtonCodes[3] = VirtualButtonCode.BTN_Y; // B4
                virtualButtonCodes[4] = VirtualButtonCode.BTN_L1; // T3
                virtualButtonCodes[6] = VirtualButtonCode.BTN_R1; // T1
                virtualButtonCodes[5] = VirtualButtonCode.BTN_L2; // T4  (virtualize as L2/R2 axis)
                virtualButtonCodes[7] = VirtualButtonCode.BTN_R2; // T2  (virtualize as L2/R2 axis)
                virtualButtonCodes[8] = VirtualButtonCode.BTN_ESC;    // 3rd function button
                virtualButtonCodes[9] = VirtualButtonCode.BTN_SELECT; // MOUSE
                virtualButtonCodes[10] = VirtualButtonCode.BTN_START; // ENTER
                break;
        }

        Controllers.addListener(new ControllerListenerAdapter() {

            private final float[] rawAxes = new float[MAX_AXES];

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
                    analogAxes[idx] = rawAxes[idx];
                }

                // android dpad analog axes
                final int DPAD_X_AXIS = 6;
                final int DPAD_Y_AXIS = 7;

                switch (GameWorld.getInstance().getControllerMode()) {
                    default:
                        // Linux USB (Ipega PG-9076)
                    case 0:
                        // L2/R2 are analog (positive-range only)
                        analogAxes[VIRTUAL_L2_AXIS] = rawAxes[2];
                        analogAxes[VIRTUAL_R2_AXIS] = rawAxes[5];
                        // set the X1 and Y1 axes
                        analogAxes[VIRTUAL_X1_AXIS] = rawAxes[3];
                        analogAxes[VIRTUAL_Y1_AXIS] = rawAxes[4];
                        break;
                    // Windows (USB, B/T)
                    case 1:
                        // swap the WS and AD axes
                        analogAxes[VIRTUAL_AD_AXIS] = rawAxes[1];
                        analogAxes[VIRTUAL_WS_AXIS] = rawAxes[0];
                        // swap the X1 and Y1 axes
                        analogAxes[VIRTUAL_X1_AXIS] = rawAxes[3];
                        analogAxes[VIRTUAL_Y1_AXIS] = rawAxes[2];
                        break;
                    // Android (B/T)
                    case 2:
                        // Android reports Dpad as axes - set it only if it was actually moved?
                        if ((DPAD_X_AXIS == axisIndex) || (DPAD_Y_AXIS == axisIndex)) {
                            analogAxes[VIRTUAL_AD_AXIS] = rawAxes[DPAD_X_AXIS];
                            analogAxes[VIRTUAL_WS_AXIS] = rawAxes[DPAD_Y_AXIS];
                        }
                        // L2/R2 are analog axes range [0:1.0]
                        analogAxes[VIRTUAL_L2_AXIS] = rawAxes[5];
                        analogAxes[VIRTUAL_R2_AXIS] = rawAxes[4];
                        break;
                    // Linux USB (BELKIN NOSTROMO n45)
                    case 3:
                        // swap the X1 and Y1 axes  todo: Windos but not Linux?
                        analogAxes[VIRTUAL_X1_AXIS] = rawAxes[3];
                        analogAxes[VIRTUAL_Y1_AXIS] = rawAxes[2];
                        break;
                }
//                print("#" + indexOf(controller) + ", rawAxes " + axisIndex + ": " + value);
                return false;
            }


        });
    }
}
