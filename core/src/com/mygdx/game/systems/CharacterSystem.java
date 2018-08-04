package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.components.CharacterComponent;

/**
 * Created by mango on 2/10/18.
 */

public class CharacterSystem extends IteratingSystem implements EntityListener {

    public CharacterSystem() {
        super(Family.all(CharacterComponent.class).get());
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
    }

    @Override
    public void entityAdded(Entity entity) {
        //empty
    }

    @Override
    public void entityRemoved(Entity entity) {
        //empty
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        CharacterComponent comp = entity.getComponent(CharacterComponent.class);

//        if (null != comp.character)
            comp.character.update(entity, deltaTime, comp.lookRay);
    }
}
