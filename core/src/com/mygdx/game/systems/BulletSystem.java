package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.components.BulletComponent;

/**
 * Created by neiderm on 12/18/17.
 */

public class BulletSystem extends IteratingSystem implements EntityListener {


    public BulletSystem() {

        super(Family.all(BulletComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) { // empty
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
        BulletWorld.getInstance().addBody(bc.body);
    }

    @Override
    public void entityRemoved(Entity entity) {

        BulletComponent bc = entity.getComponent(BulletComponent.class);

        // assert null != bc
        // assert null != bc.body
        BulletWorld.getInstance().removeBody(bc.body);
        bc.body.dispose();
    }
}
