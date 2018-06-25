package com.mygdx.game.controllers;

import com.mygdx.game.controllers.CharacterControlAuto;

/**
 * Created by mango on 2/10/18.
 */

public interface CharacterControlManual extends CharacterControlAuto {

    /*
    this would change to something generic like Object
     */
    public void inputSet(float x, float y);
}
