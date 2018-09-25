package com.mygdx.game.characters;

import com.badlogic.gdx.math.Vector3;

/**
 * Created by utf1247 on 6/28/2018.
 */

class InputStruct {

    public ButtonsEnum buttonPress = InputStruct.ButtonsEnum.BUTTON_NONE;
    private Vector3 inpVector = new Vector3();


    public InputStruct set(Vector3 touchPadCoords, ButtonsEnum button) {

        this.inpVector.set(touchPadCoords);
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
