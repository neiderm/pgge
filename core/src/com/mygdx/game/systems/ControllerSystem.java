package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.components.ControllerComponent;
import com.mygdx.game.controllers.ICharacterControlAuto;

/**
 * Created by mango on 2/10/18.
 */

public class ControllerSystem extends IteratingSystem {

    public ControllerSystem() {
        super(Family.all(ControllerComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        ICharacterControlAuto ctrl = entity.getComponent(ControllerComponent.class).controller;

        if (null != ctrl)
            ctrl.update(deltaTime);
    }

}
