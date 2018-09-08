package com.mygdx.game.characters;

import com.badlogic.gdx.math.Vector3;

/**
 * Created by utf1247 on 6/28/2018.
 */

class InputStruct {

    public ButtonsEnum buttonPress;
    private Vector3 inpVector;


    InputStruct(Vector3 touchPadCoords, ButtonsEnum button){
        set(touchPadCoords, button);
    }

    public InputStruct set(Vector3 touchPadCoords, ButtonsEnum button) {

        this.inpVector = touchPadCoords;
        this.buttonPress = button;
        return this;
    }

    public float getLinearDirection(){ return inpVector.z; }

    public float getAngularDirection(){ return inpVector.x; }

    public enum ButtonsEnum {
        BUTTON_NONE,
        BUTTON_A,
        BUTTON_B,
        BUTTON_C
    }
}
