package com.mygdx.game.Components;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.actors.GameCharacter;
import com.mygdx.game.controllers.CharacterControlAuto;

/**
 * Created by mango on 2/10/18.
 */

/*
  instantiate with instance of any suitable simple controller e.g. PID controller etc.
 */
public class ControllerComponent implements Component {

    public CharacterControlAuto controller;

    public ControllerComponent(CharacterControlAuto controller) {
        this.controller = controller;
    }

}
