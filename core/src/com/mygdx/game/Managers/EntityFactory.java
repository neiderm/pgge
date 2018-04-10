package com.mygdx.game.Managers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.EntityBuilder;
import com.mygdx.game.GameObject;

//import static com.mygdx.game.GameObject.loadKinematicEntity;

/**
 * Created by neiderm on 12/21/2017.
 *
 * IDFK
 *
 EntityFactory<GameObject> dynFactory = new EntityFactory<GameObject>();

 engine.addEntity(dynFactory.createS(new BoxObject(boxTemplateModel, new Vector3(40f, 2f, 40f)), new Vector3(0, -4 + yTrans, 0)));
 engine.addEntity(dynFactory.createS(new SphereObject(sphereTemplateModel, 16), new Vector3(10, 5 + yTrans, 0)));
 engine.addEntity(dynFactory.createS(new BoxObject(primitivesModel, "box", new Vector3(4f, 1f, 4f)), new Vector3(0, 10, -5)));
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
//        return loadKinematicEntity(
//                object.model, object.rootNodeId, object.shape, translation, object.size);
        return new Entity();
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
