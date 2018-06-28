package com.mygdx.game.controllers;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by utf1247 on 6/28/2018.
 */

public class InputStruct {

    ButtonsEnum buttonPress;
    Vector2 inpVector;

    public InputStruct set(Vector2 touchPadCoords, ButtonsEnum button) {

        this.inpVector = touchPadCoords;
        this.buttonPress = button;
        return this;
    }

    public enum ButtonsEnum {
        BUTTON_NONE,
        BUTTON_A,
        BUTTON_B,
        BUTTON_C
    }
}
