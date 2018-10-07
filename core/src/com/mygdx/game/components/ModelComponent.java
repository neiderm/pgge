package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * Created by mango on 12/21/17.
 */

public class ModelComponent implements Component {

    public ModelInstance modelInst;
    public float boundingRadius = 0;
    public boolean isShadowed = true;
    public Vector3 center = new Vector3();

    public int id = 0;
    private static int instcnt = 0;


    public ModelComponent(ModelInstance instance) {

        Vector3 dimensions = new Vector3();
        BoundingBox boundingBox;

        this.id = ++instcnt;

        this.modelInst = instance;

        boundingBox = instance.calculateBoundingBox(new BoundingBox());
        boundingBox.getDimensions(dimensions);
        boundingBox.getCenter(center);
        boundingRadius = dimensions.len() / 2f;
    }
}
