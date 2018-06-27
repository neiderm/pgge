package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.characters.IGameCharacter;
import com.mygdx.game.controllers.ICharacterControlAuto;

/**
 * Created by mango on 2/10/18.
 */

public class CharacterSystem extends IteratingSystem {

    public CharacterSystem() {
        super(Family.all(CharacterComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        ICharacterControlAuto ctrl = entity.getComponent(CharacterComponent.class).controller;

        if (null != ctrl)
            ctrl.update(deltaTime);

        IGameCharacter actor = entity.getComponent(CharacterComponent.class).actor;

        if (null != actor) {
            actor.update(deltaTime);
        }
    }

}
