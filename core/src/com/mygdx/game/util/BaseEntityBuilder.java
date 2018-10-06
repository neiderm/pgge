package com.mygdx.game.util;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.components.ModelComponent;

/**
 * Created by mango on 4/1/18.
 */

public class BaseEntityBuilder extends EntityBuilder  {

    BaseEntityBuilder() {
    }

    BaseEntityBuilder(Model model, Vector3 size) {
        this.model = model;
        this.size = size;
    }

    @Override
    public Entity create(float mass, Vector3 trans, Vector3 size) {
        return null;//new Entity(); // useless
    }

    public static Entity load(Model model, String rootNodeId){
        // we can set trans default value as do-nothing 0,0,0 so long as .trn() is used (adds offset onto present trans value)
        return load(model, rootNodeId, new Vector3(1, 1, 1), new Vector3(0, 0, 0));
    }

    public static Entity load(Model model, String rootNodeId, Vector3 size, Vector3 translation)
    {
        Entity e = new Entity();

        if (null != rootNodeId) {
            ModelInstance instance = ModelInstanceEx.getModelInstance(model, rootNodeId);
            e.add(new ModelComponent(instance, size));
        } else {
            e.add(new ModelComponent(model, size));
        }

        // leave translation null if using translation from the model layout 
        if (null != translation) {
            e.getComponent(ModelComponent.class).modelInst.transform.trn(translation);
        }

        return e;
    }
}
