package com.mygdx.game.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.utils.ArrayMap;

/**
 * Created by neiderm on 6/28/2018.
 */
/*
   Thanks to https://www.asciiart.eu/computers/joysticks
             https://www.asciiart.eu/computers/game-consoles
      _____________________________
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

public /* abstract */ class InputStruct implements CtrlMapperIntrf {


    private ArrayMap<ButtonsEnum, ButtonData> buttonsTable = new ArrayMap<ButtonsEnum, ButtonData>();

    private InputStruct.ButtonData buttonsData = new InputStruct.ButtonData();


    public InputStruct() {

        connectedCtrl = getConnectedCtrl(0);
    }

    @Override
    public void update(float deltaT) { // mt
    }


    public class ButtonData {

        int value;
        boolean isRepeatable;

        ButtonData setValue(int value, boolean isRepeatable) {
            this.value = value;
            this.isRepeatable = isRepeatable;
            return this;
        }
    }


    void buttonSet(ButtonsEnum key, int value, boolean isRepeated) {

        buttonsTable.put(key, buttonsData.setValue(value, isRepeated));
    }

    public enum ButtonsEnum { // idfk
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
    protected float getAxisX(int axisIndex) {

        return analogAxes.x;
    }

    // get the "virtual axis"
    protected float getAxisY(int axisIndex) {

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


    private Controller connectedCtrl;

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


    public enum InputState {
        INP_NONE,
        INP_SELECT,
        INP_BACK,
        INP_JUMP
    }


    private InputState inputState;

    public InputState getInputState() {

        return getInputState(true);
    }

    /*
     * checkisTouched: false if caller is handling the touch event
     */
    public InputState getInputState(boolean checkIsTouched) {

        InputState rv = inputState;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {

            rv = InputState.INP_BACK;

        } else if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || getControlButton(Input.Buttons.LEFT)) {
            // A (MYGT-Y)
            rv = InputState.INP_SELECT;

        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || getControlButton(Input.Buttons.RIGHT)) {
            // B
            rv = InputState.INP_JUMP;
        } else if (getControlButton(Input.Buttons.BACK)) {
            Gdx.app.log("InputStruct", "Buttons.BACK");    // Ipega "Y"    Belkin "B4" MYGT "Y"
        } else if (getControlButton(Input.Buttons.FORWARD) ) {
            Gdx.app.log("InputStruct", "Buttons.FORWARD"); // L1
        } else if (getControlButton(Input.Buttons.MIDDLE) ) {
            Gdx.app.log("InputStruct", "Buttons.MIDDLE");  // Ipega "X"  Belkin "B3" MYGT "A"
        } else if (Gdx.input.justTouched()) {

            if (checkIsTouched) {
                rv = InputState.INP_SELECT;
            }
        } else {
//            rv = InputState.INP_NONE; // no-op
        }
        inputState = InputState.INP_NONE; // unlatch the input state
        return rv;
    }

    public void setInputState(InputState inputState) {

        this.inputState = inputState;
    }

    private PovDirection getControlPov(/*int povCode*/) {

        PovDirection d = PovDirection.center;
        if (null != connectedCtrl) {
            d = connectedCtrl.getPov(0);
        }
        return d;
    }

    private boolean getControlButton(int button) {

        boolean rv = false;
        if (null != connectedCtrl) {
            rv = connectedCtrl.getButton(button);
        }
        return rv;
    }

    public class DpadAxis {
        int x;
        int y;

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

    public class AnalogAxis {
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
    public DpadAxis getDpad(DpadAxis asdf) {

        dPadAxes.clear();

//        PovDirection povDir = getControlPov();
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
}
