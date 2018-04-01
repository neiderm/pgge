package com.mygdx.game.Managers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.EntityBuilder;
import com.mygdx.game.GameObject;

/**
 * Created by neiderm on 12/21/2017.
 */

public /* abstract */ class EntityFactory<T extends GameObject> {

    // model? not sure what other instance data

//        T object;

    public EntityFactory() {
    }
/*
        EntiteeFactory(T object){
            this.object = object;
        }
*/
/*
    private Entity create(T object, float mass, Vector3 translation) {
        return object.create(mass, translation);
    }
*/
    public Entity create(T object, Vector3 translation) {
//        object.create();
        return EntityBuilder.loadKinematicEntity(
                object.model, object.rootNodeId, object.shape, translation, object.size);
//        return new Entity();
    }


    public static class DynEntFact<T extends GameObject> extends EntityFactory {
        /*
                DynEntFact(T object){
                    super(object);
                }
        */
/*        public Entity create(T object, Vector3 translation) {
            object.create();
            return new Entity();
        }*/
    }
}
