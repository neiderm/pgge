package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.components.BulletComponent;

/**
 * Created by neiderm on 12/18/17.
 */

public class BulletSystem extends IteratingSystem {


    public BulletSystem() {

        super(Family.all(BulletComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) { // empty
    }

    @Override
    public void addedToEngine(Engine engine) {

        super.addedToEngine(engine);
    }

    @Override
    public void removedFromEngine(Engine engine) {

        for (Entity e : getEntities()) {

            BulletComponent bc = e.getComponent(BulletComponent.class);

            // assert null != bc
            // assert null != bc.shape
            // assert null != bc.body

            BulletWorld.getInstance().removeBody(bc.body);

            if (null != bc.motionstate) {
                bc.motionstate.dispose();
            }
            bc.shape.dispose();
            bc.body.dispose();
        }
    }
}
