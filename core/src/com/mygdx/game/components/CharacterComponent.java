package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.collision.Ray;
import com.mygdx.game.characters.IGameCharacter;
import com.mygdx.game.controllers.ICharacterControlAuto;
import com.mygdx.game.util.GameEvent;

/**
 * Created by mango on 2/10/18.
 */

/*
 the custom "character controller" class (i.e. for non-dynamic collision objects) would be able
 to instantiate with inserted instance of a suitable simple controller e.g. PID controller etc.
 */
public class CharacterComponent implements Component {

    public GameEvent gameEvent;
    public IGameCharacter character;
    public ICharacterControlAuto controller;
    public Ray lookRay = new Ray();


    public CharacterComponent(IGameCharacter character) {
        this.character = character;
    }

    /*
     every entity instance must have its own gameEvent instance
     */
    public CharacterComponent(IGameCharacter character, GameEvent gameEvent) {

        this(character);

        this.gameEvent = gameEvent;
    }
}
