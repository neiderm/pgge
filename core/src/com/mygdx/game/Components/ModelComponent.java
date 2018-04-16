package com.mygdx.game.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;


/**
 * Created by mango on 12/21/17.
 */

public class ModelComponent implements Component {

    public ModelInstance modelInst;
    public Vector3 scale;
    public float boundingRadius = 0;
    public Vector3 center = new Vector3(); // idfk
    public boolean isShadowed = true;


    public ModelComponent(Model model, Vector3 scale) {

        this(new ModelInstance(model), scale);
    }

    public ModelComponent(ModelInstance instance, Vector3 scale) {

        this.modelInst = instance;

        if (null != scale) {
            this.scale = new Vector3(scale);
        }
        else{
//            this.scale = new Vector3(1, 1, 1); // I wonder if this has any ramification?
        }
/*
        BoundingBox boundingBox = new BoundingBox();
        center = new Vector3();
        Vector3 dimensions = new Vector3();
        this.modelInst.calculateBoundingBox(boundingBox);
        boundingBox.getCenter(center);
        boundingBox.getDimensions(dimensions);
        boundingRadius = dimensions.len() / 2f;
*/
    }
}
