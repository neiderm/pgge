package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.mygdx.game.Components.CharacterComponent;
import com.mygdx.game.Components.ModelComponent;

/**
 * Created by mango on 2/10/18.
 */

public class CharacterSystem extends EntitySystem implements EntityListener {

    private ImmutableArray<Entity> entities;


    @Override
    public void entityAdded(Entity entity) {
    }

    @Override
    public void entityRemoved(Entity entity) {
    }

    @Override
    public void addedToEngine(Engine engine) {

        // Grabs all entities with desired components
        entities = engine.getEntitiesFor(Family.all(CharacterComponent.class).get());

        engine.addEntityListener(Family.all(CharacterComponent.class).get(), this);
    }

    @Override
    public void update(float delta) {

        for (Entity e : entities) {

            CharacterComponent charComp = e.getComponent(CharacterComponent.class);

            charComp.controller.update();
        }
    }
}

