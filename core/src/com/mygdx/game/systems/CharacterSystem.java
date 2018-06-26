package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.Components.CharacterComponent;
import com.mygdx.game.actors.GameCharacter;
import com.mygdx.game.controllers.CharacterControlAuto;

/**
 * Created by mango on 2/10/18.
 */

public class CharacterSystem extends IteratingSystem {

    public CharacterSystem() {
        super(Family.all(CharacterComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        CharacterControlAuto ctrl = entity.getComponent(CharacterComponent.class).controller;

        if (null != ctrl)
            ctrl.update(deltaTime);

        GameCharacter actor = entity.getComponent(CharacterComponent.class).actor;

        if (null != actor) {
            actor.update(deltaTime);
        }
    }

}
