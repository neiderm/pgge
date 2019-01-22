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
/*   My fancy controller -perspective drawing ;)
      _____________________________
     /  [5]                  [7]  /
    /  [4]                  [6]  /
    -----------------------------
   / +                 [3]     /
  /    [8][8][10]  [2]  [1]  /
 /                   [0]    /
/          _____          /
/      +  /     \     +  /
---------/       \------/

The input filtering done in here could be integrated into the "simple vehicle model"
A "non-simple differential tracked vehicile model" could inherit and use this same
input filtering inherited from simple vehicle model.
A "flying vehicle model" would override this input mapping as it would need to
support roll, but would not have i.e. reverse/gear-shifting capability.

There is stateful-ness in the input handling as, action buttons must have their
state latched in order to be pollable (e.g. hat switch, axes tend to be held down and don't
need latched)
Also if you want to create e.g. a "virtual stick shifter", that is a state e.g. like
"D2 D1 N R"


control states
virtual shifter DNR has state
buttons set latched state so they can be polled (hat/axis tend to be held down)

could move setaxis and related bits over to "simple vehicle control model" .

we can have different control schemes for the same underlying model e.g. differential axes
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

        } else if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || getControlButton(Input.Buttons.LEFT) /* left == "X Button" ?  */) {

            rv = InputState.INP_SELECT;

        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || getControlButton(Input.Buttons.RIGHT) /* Circle / "B"  */) {

            rv = InputState.INP_JUMP;
        }
        else if (getControlButton(Input.Buttons.LEFT) /*   */) {
        }
        else if (getControlButton(Input.Buttons.BACK) /* Triangle / "Y"  */) {
        }
        else if (getControlButton(Input.Buttons.FORWARD) /*   */) {
        }
        else if (getControlButton(Input.Buttons.MIDDLE) /* Square / "X"   */) {
        }

        else if (Gdx.input.justTouched()) {

            if (checkIsTouched) {
                rv = InputState.INP_SELECT;
            }
        } else {
            rv = InputState.INP_NONE; // no-op
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
        void clear(){
            x = 0;
            y = 0;
        }
        public int getX(){ return x; }
        public int getY(){ return y; }
    }

    public class AnalogAxis {
        float x;
        float y;
        void clear(){
            x = 0;
            y = 0;
        }
        public float getX(){ return x; }
        public float getY(){ return y; }
    }

    private DpadAxis dPadAxes = new DpadAxis(); // typically only 1 dPad, but it could be implemented as either an axis or 4 buttons while libGdx has it's own abstraction
    private AnalogAxis analogAxes = new AnalogAxis(); // would need array of max analog axes, for now just use one

    /*
     * "virtual dPad" provider using either controller POV or keyboard U/D/L/R
     *
     * NOTE: Android emulator: it gets keyboard input surprisingly on Windows (but not Linux it seems).
     * But glitchy and not worth considering.
     */
    public DpadAxis getDpad(DpadAxis asdf){

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
