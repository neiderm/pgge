package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.controllers.SteeringBulletEntity;
import com.mygdx.game.controllers.SteeringTankController;
import com.mygdx.game.controllers.TankController;

/**
 * Created by neiderm on 2/10/18.
 */

public class CharacterSystem extends IteratingSystem /*implements EntityListener */ {

    public CharacterSystem() {

        super(Family.all(CharacterComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {

        if (!GameWorld.getInstance().getIsPaused()) {  // would like to allow engine to be actdive if ! paused but on-screen menu is up
            for (Entity e : getEntities())
                processEntity(e, deltaTime);
        }
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        CharacterComponent cc = entity.getComponent(CharacterComponent.class);

        // if steerable is valid, update() it, ...

        if (null != cc.steerable) {

            entity.getComponent(CharacterComponent.class).steerable.update(deltaTime);
        } else {
            // spin up a new steerable

            BulletComponent bc = entity.getComponent(BulletComponent.class);
            if (null != bc) {

                TankController tc = new TankController(bc.body, bc.mass); /* should be a property of the tank? */

                cc.setSteerable(
                        new SteeringTankController(tc, bc.body, new SteeringBulletEntity(bc.body)));
            }
        }
    }
}
