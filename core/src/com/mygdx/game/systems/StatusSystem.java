package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.components.StatusComponent;

/**
 * Created by utf1247 on 7/5/2018.
 */

public class StatusSystem extends IteratingSystem {

    public StatusSystem() {
        super(Family.all(StatusComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        StatusComponent comp = entity.getComponent(StatusComponent.class);

//        if (null != comp) 
        {
            if (null != comp.statusUpdater) {
                comp.statusUpdater.update(entity);
            }
        }
    }
}
