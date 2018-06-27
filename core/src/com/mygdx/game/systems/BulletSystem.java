package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.components.BulletComponent;

/**
 * Created by mango on 12/18/17.
 * a bullet and libgdx test from
 * "from http://bedroomcoders.co.uk/libgdx-bullet-redux-2/"
 */

public class BulletSystem extends IteratingSystem implements EntityListener {

    private BulletWorld world;


    public BulletSystem(BulletWorld world) {

        super(Family.all(BulletComponent.class).get());
        this.world = world;
    }

    protected void processEntity(Entity entity, float deltaTime) { // empty
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        world.update(deltaTime);
    }


    @Override
    public void addedToEngine(Engine engine) {

        super.addedToEngine(engine);

        // listener for these so that their bullet objects can be dispose'd
        engine.addEntityListener(getFamily(), this);
    }

    @Override
    public void removedFromEngine(Engine engine) {

        engine.removeEntityListener(this); // Ashley bug (doesn't remove listener when system removed?

        world.dispose();

        for (Entity e : getEntities()) {

            BulletComponent bc = e.getComponent(BulletComponent.class);

            // assert null != bc
            // assert null != bc.shape
            // assert null != bc.body
            bc.shape.dispose();
            bc.body.dispose();
        }
    }

    @Override
    public void entityAdded(Entity entity) {

        BulletComponent bc = entity.getComponent(BulletComponent.class);
        //assert null != bc
        //assert null != bc.body
        world.addBody(bc.body);
    }

    @Override
    public void entityRemoved(Entity entity) {

        BulletComponent bc = entity.getComponent(BulletComponent.class);

        // assert null != bc
        // assert null != bc.body
        world.removeBody(bc.body);
    }
}
