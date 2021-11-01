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

/**
 * Created by neiderm on 6/28/2018.
 */
/*
Tested Controllers:

-----------------------------
Belkin Nostromo n45 (USB)
"Nostromo n45 Dual Analog Gamepad"  (Linux)
"Nostromo N45"  (Win 10)

-----------------------------
IPEGA PG-9076
"ShanWan PS3/PC Wired GamePad"  (USB Win 10)
"X360 Controller"  (USB Linux)
"SHANWAN PS3/PC Gamepad"  (2.4 gHz Linux)
"Ipega PG-9069 - Bluetooth Gamepad"  (B/T Linux)
"PG-9076"  (B/T Android)                     retest

-----------------------------
MYGT MY-C04
"ShanWan PS3/PC Wired GamePad"  (Win10 2.4 gHz)
"MYGT Controller"  (Android)
 note: 2.4 Gz or B/T (USB port is charging only)

-----------------------------
Sony Wireless Controller
"PLAYSTATION(R)3 Controller" (USB WIN10)    retest
"PS3 Controller"  (USB Linux - after plug USB, LEDs [1:4] flash, push PS button, LED 1 solid, connected)
"PS3 Controller"  (B/T Linux)
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


    InputMapper() {
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
            if (VirtualButtonCode.BTN_UP == vbCode) {
                analogAxes[VIRTUAL_WS_AXIS] = newButtonState ? (-1) : 0;
            }
            if (VirtualButtonCode.BTN_DOWN == vbCode) {
                analogAxes[VIRTUAL_WS_AXIS] = newButtonState ? 1 : 0;
            }

            switch (GameWorld.getInstance().getControllerMode()) {
                default:
                case 0:
                case 1:
                case 2:
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
    private boolean getControlButton(VirtualButtonCode vbutton, boolean letRepeat, int repeatPeriod) {

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
            case 0:
            case 1:
            case 3:
                virtualButtonCodes[0] = VirtualButtonCode.BTN_A;
                virtualButtonCodes[1] = VirtualButtonCode.BTN_B;
                virtualButtonCodes[2] = VirtualButtonCode.BTN_X;
                virtualButtonCodes[3] = VirtualButtonCode.BTN_Y;

                virtualButtonCodes[4] = VirtualButtonCode.BTN_SELECT; // Select (view)
                virtualButtonCodes[6] = VirtualButtonCode.BTN_START; // Start (menu)

                virtualButtonCodes[9] = VirtualButtonCode.BTN_L1;
                virtualButtonCodes[10] = VirtualButtonCode.BTN_L2;

                virtualButtonCodes[11] = VirtualButtonCode.BTN_UP;
                virtualButtonCodes[12] = VirtualButtonCode.BTN_DOWN;
                virtualButtonCodes[13] = VirtualButtonCode.BTN_LEFT;
                virtualButtonCodes[14] = VirtualButtonCode.BTN_RIGHT;
                break;
            case 2: // Android
                virtualButtonCodes[19] = VirtualButtonCode.BTN_UP;
                virtualButtonCodes[20] = VirtualButtonCode.BTN_DOWN;
                virtualButtonCodes[21] = VirtualButtonCode.BTN_LEFT;
                virtualButtonCodes[22] = VirtualButtonCode.BTN_RIGHT;

                virtualButtonCodes[96] = VirtualButtonCode.BTN_A;
                virtualButtonCodes[97] = VirtualButtonCode.BTN_B;
                virtualButtonCodes[99] = VirtualButtonCode.BTN_X;
                virtualButtonCodes[100] = VirtualButtonCode.BTN_Y;

                virtualButtonCodes[102] = VirtualButtonCode.BTN_L1;
                virtualButtonCodes[103] = VirtualButtonCode.BTN_R1;
                virtualButtonCodes[109] = VirtualButtonCode.BTN_SELECT; // Back
                virtualButtonCodes[108] = VirtualButtonCode.BTN_START; // Start
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

                switch (GameWorld.getInstance().getControllerMode()) {
                    default:
                    case 0:
                    case 1:
                    case 3:
                        break;
                    case 2:
                        // Android: swap the L2 and R2 axes
                        analogAxes[VIRTUAL_L2_AXIS] = rawAxes[5];
                        analogAxes[VIRTUAL_R2_AXIS] = rawAxes[4];
                        break;
                }
//                print("#" + indexOf(controller) + ", rawAxes " + axisIndex + ": " + value);
                return false;
            }
        });
    }
}
