package com.mygdx.game.util;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.components.ModelComponent;

/**
 * Created by mango on 4/1/18.
 */

public class BaseEntityBuilder extends EntityBuilder {

    BaseEntityBuilder() {
    }

    @Override
    public Entity create(float mass, Vector3 trans, Vector3 size) {
        return null;//new Entity(); // useless
    }

    public static Entity load(Model model, String rootNodeId) {
        // we can set trans default value as do-nothing 0,0,0 so long as .trn() is used (adds offset onto present trans value)
        return load(model, rootNodeId, new Vector3(1, 1, 1), new Vector3(0, 0, 0));
    }

    public static Entity load(Model model, String rootNodeId, Vector3 size, Vector3 translation) {
        Entity e = new Entity();
        ModelInstance instance;
        if (null != rootNodeId) {
            instance = ModelInstanceEx.getModelInstance(model, rootNodeId);
        } else {
            instance = new ModelInstance(model);
        }


        if (null != size) {
// https://stackoverflow.com/questions/21827302/scaling-a-modelinstance-in-libgdx-3d-and-bullet-engine
            // note : modelComponent creating bouding box
            instance.nodes.get(0).scale.set(size);
            instance.calculateTransforms();
        }

        e.add(new ModelComponent(instance));


        // leave translation null if using translation from the model layout 
        if (null != translation) {
            e.getComponent(ModelComponent.class).modelInst.transform.trn(translation);
        }

        return e;
    }
}
