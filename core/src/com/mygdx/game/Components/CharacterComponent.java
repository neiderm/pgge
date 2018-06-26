package com.mygdx.game.Components;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.actors.GameCharacter;
import com.mygdx.game.controllers.CharacterControlAuto;

/**
 * Created by mango on 2/10/18.
 */

/*
 the custom "character controller" class (i.e. for non-dynamic collision objects) would be able
 to instantiate with inserted instance of a suitable simple controller e.g. PID controller etc.
 */
public class CharacterComponent implements Component {

    public GameCharacter actor;

    public CharacterComponent(GameCharacter actor){
        this.actor = actor;
    }

    public CharacterComponent(CharacterControlAuto controller) {
        this.controller = controller;
    }

    public CharacterControlAuto controller;
}
