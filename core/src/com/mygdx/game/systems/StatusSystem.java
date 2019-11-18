package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.StatusComponent;

/**
 * Created by neiderm on 7/5/2018.
 */

public class StatusSystem extends IteratingSystem {

    public StatusSystem() {

        super(Family.all(StatusComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        if (!GameWorld.getInstance().getIsPaused()) {  // would like to allow engine to be actdive if ! paused but on-screen menu is up

            StatusComponent comp = entity.getComponent(StatusComponent.class);

            if (null != comp) {
/*
                if (comp.lifeClock > 0) {

                    comp.lifeClock -= 1;
                }
*/
                if (0 == comp.lifeClock) {
//                            // can be removed ...
                    comp.deleteMe = true;
                }
            }
        }
    }
}
