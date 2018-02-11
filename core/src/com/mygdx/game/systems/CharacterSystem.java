package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Components.CharacterComponent;
import com.mygdx.game.Components.ModelComponent;

/**
 * Created by mango on 2/10/18.
 */

public class CharacterSystem extends EntitySystem implements EntityListener {

    // TODO: needs to be a collection, not a single instance!
    CharacterComponent charComp;

    @Override
    public void entityAdded(Entity entity) {

        charComp = entity.getComponent(CharacterComponent.class);
        // get a reference to model comp transform so we don't have to keep asking the
        // model instance for it
        charComp.transform = entity.getComponent(ModelComponent.class).modelInst.transform;

//        entity.getComponent(ModelComponent.class).modelInst.transform.getTranslation(charComp.translation);
    }

    @Override
    public void entityRemoved(Entity entity) {
    }

    @Override
    public void addedToEngine(Engine engine) {

        engine.addEntityListener(Family.all(CharacterComponent.class).get(), this);
    }

    private Vector3 translation = new Vector3(); // tmp working variable

    @Override
    public void update(float delta) {

        // we could keep the model translation a class state variable so we wouldn't
        // have to keep getting it from model instance. however, that seems like a bad idea just
        // in case there was another process out there manipulating the model transformation, then
        // our translation data could get out of sync.

        translation = charComp.transform.getTranslation(translation);
        translation.add(charComp.controller.doControl(translation));
        charComp.transform.setTranslation(translation);
    }
}

