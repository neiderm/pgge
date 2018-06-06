package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.Components.CharacterComponent;

/**
 * Created by mango on 2/10/18.
 */

public class CharacterSystem extends IteratingSystem {

    public CharacterSystem() {
        super(Family.all(CharacterComponent.class).get());
    }

    @Override
    protected void processEntity (Entity entity, float deltaTime){

        for (Entity e : getEntities()) {

            CharacterComponent charComp = e.getComponent(CharacterComponent.class);

            charComp.controller.update();
        }
    }
}
