package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.controllers.ICharacterControlAuto;

/**
 * Created by mango on 2/10/18.
 */

/*
  instantiate with instance of any suitable simple controller e.g. PID controller etc.
 */
public class ControllerComponent implements Component {

    public ICharacterControlAuto controller;

    public ControllerComponent(ICharacterControlAuto controller) {
        this.controller = controller;
    }

}
