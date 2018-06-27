package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.characters.IGameCharacter;
import com.mygdx.game.controllers.ICharacterControlAuto;

/**
 * Created by mango on 2/10/18.
 */

/*
 the custom "character controller" class (i.e. for non-dynamic collision objects) would be able
 to instantiate with inserted instance of a suitable simple controller e.g. PID controller etc.
 */
public class CharacterComponent implements Component {

    public IGameCharacter actor;

    public CharacterComponent(IGameCharacter actor){
        this.actor = actor;
    }

    public ICharacterControlAuto controller;
}
