package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
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

    private Entity player;

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

        if (cc.isPlayer) {
            player = entity;
        } else {
            cc.isPlayer = false; // debug
        }

        if (null != player) {
            if (null != cc.steerable) {

                entity.getComponent(CharacterComponent.class).steerable.update(deltaTime);

            } else if (!cc.isPlayer) {

                btRigidBody rb = entity.getComponent(BulletComponent.class).body;

                TankController tc = new TankController(rb, entity.getComponent(BulletComponent.class).mass); /* should be a property of the tank? */

                cc.setSteerable(
                        new SteeringTankController(
                                tc, rb, new SteeringBulletEntity(player.getComponent(BulletComponent.class).body)));
            }
        }
    }
}
