package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.FeatureComponent;


public class FeatureSystem extends IteratingSystem {

    public FeatureSystem() {

        super(Family.all(FeatureComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        if (!GameWorld.getInstance().getIsPaused()) {  // would like to allow engine to be actdive if ! paused but on-screen menu is up

            FeatureComponent comp = entity.getComponent(FeatureComponent.class);

            if (null != comp) {

                if (null != comp.featureAdpt) {
                    comp.featureAdpt.update(entity);
                }
            }
        }
    }
}
