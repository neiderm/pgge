package com.mygdx.game.characters;

import com.badlogic.gdx.utils.ArrayMap;

/**
 * Created by utf1247 on 6/28/2018.
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


control states
virtual shifter DNR has state
buttons set latched state so they can be polled (hat/axis tend to be held down)

could move setaxis and related bits over to "simple vehicle control model" .

we can have different control schemes for the same underlying model e.g. differential axes
 */
public abstract class InputStruct implements CtrlMapperIntrf {

    private float angularD = 0f;
    private float linearD = 0f;

    private ArrayMap<ButtonsEnum, ButtonData> buttonsTable = new ArrayMap<ButtonsEnum, ButtonData>();

    private InputStruct.ButtonData buttonsData = new InputStruct.ButtonData();


    InputStruct(){

        buttonSet( InputStruct.ButtonsEnum.BUTTON_1, 0, false );
    }


    public class ButtonData {

        int value;
        boolean isRepeatable;

        ButtonData setValue(int value, boolean isRepeatable){
            this.value = value;
            this.isRepeatable = isRepeatable;
            return this;
        }
    }


    private int buttonGet(ButtonsEnum key) {

        ButtonData data = buttonsTable.get(key);

        int value = data.value;
        if (!data.isRepeatable)
            buttonSet(key, 0, false /* hmmmm */ ); // delatch it

        return value;
    }

    void buttonSet(ButtonsEnum key, int value, boolean isRepeated){

        buttonsTable.put(key, buttonsData.setValue(value, isRepeated));
    }

    int jumpButtonGet(){
        return buttonGet(ButtonsEnum.BUTTON_1);
    }


    float getLinearDirection(){ return linearD; }

    float getAngularDirection(){ return angularD; }

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

    /*
     reverse, e.g. stick back, or ...... (inReverse+gasApplied) ...both sticks back
     */
    public int getReverse(){return 0;}

    public int getForward(){return 0;}

//    private Object UIPictureDiagram; // idfk

    /*
      axisIndex: index of changed axes (only has value for a real axes?
      values[]: values of all 4 axes (only have all 4 if there are 2 analog mushrooms)
      Otherwise, virtual axes e.g. POV, WASD only have 2 axes.
                            -1.0
                       -1.0   +   +1.0
                            + 1.0
     */
    void setAxis(int axisIndex, float[] values){

        final float DZ = 0.25f; // actual number is irrelevant if < deadzoneRadius of TouchPad

        // rotate by a constant rate according to stick left or stick right.
        angularD = 0f;
        linearD = 0f;

        float knobX = values[0];
        float knobY = -values[1];  //   <---- note negative sign

        if (knobX < -DZ) {
            angularD = +1f;  // left (ccw)
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

    }
}
