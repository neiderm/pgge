package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.Components.BulletComponent;

/**
 * Created by mango on 12/18/17.
 * a bullet and libgdx test from
 * "from http://bedroomcoders.co.uk/libgdx-bullet-redux-2/"
 */

public class BulletSystem extends EntitySystem implements EntityListener {


    //    private Engine engine;
    private ImmutableArray<Entity> entities;
    private BulletWorld world;


    public BulletSystem(Engine engine, PerspectiveCamera cam, BulletWorld world) {

        this.world = world;
    }

    @Override
    public void update(float deltaTime) {

        world.update(deltaTime);
    }


    @Override
    public void addedToEngine(Engine engine) {

//        this.engine = engine;

        // Grabs all entities with desired components
        entities = engine.getEntitiesFor(Family.all(BulletComponent.class).get());

        // listener for these so that their bullet objects can be dispose'd
        engine.addEntityListener(Family.all(BulletComponent.class).get(), this);
    }

    @Override
    public void removedFromEngine(Engine engine) {

        engine.removeEntityListener(this); // Ashley bug (doesn't remove listener when system removed?

        world.dispose();


        // tmp ... loop all Bullet entities to destroy resources
        for (Entity e : entities) {

            BulletComponent bc = e.getComponent(BulletComponent.class);

            if (null != bc && null != bc.shape && null != bc.body) {
                bc.shape.dispose();
                bc.body.dispose();
            }
        }
    }

    @Override
    public void entityAdded(Entity entity) {

        BulletComponent bc = entity.getComponent(BulletComponent.class);

        if (null != bc) {
            if (null != bc.body) {
                world.collisionWorld.addRigidBody(bc.body);
            }
        }
    }

    @Override
    public void entityRemoved(Entity entity) {

        BulletComponent bc = entity.getComponent(BulletComponent.class);

        if (null != bc) {
            if (null != bc.body) {
                world.collisionWorld.removeRigidBody(bc.body);
            }
        }
    }
}
