package com.mygdx.game.characters;

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
 */
public class InputStruct {

    public int buttonPress = 0; // InputStruct.ButtonsEnum.BUTTON_NONE;

    private float angularD = 0f;
    private float linearD = 0f;


    public InputStruct buttonSet(int button) {

        this.buttonPress = button;
        return this;
    }

    public float getLinearDirection(){ return linearD; }

    public float getAngularDirection(){ return angularD; }

    public enum ButtonsEnum { // idfk
/*        BUTTON_NONE,
        BUTTON_1,
        BUTTON_2,
        BUTTON_3,
        BUTTON_4,
        BUTTON_5,
        BUTTON_6,
        BUTTON_7,
        BUTTON_8,
        BUTTON_9,*/
        BUTTON_10
    }


    /*
      axisIndex: index of changed axes (only has value for a real axes?
      values[]: values of all 4 axes (only have all 4 if there are 2 analog mushrooms)
      Otherwise, virtual axes e.g. POV, WASD only have 2 axes.
                            -1.0
                       -1.0   +   +1.0
                            + 1.0
     */
    public void setAxis(int axisIndex, float[] values){

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
