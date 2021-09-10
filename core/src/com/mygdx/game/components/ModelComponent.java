package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.mygdx.game.animations.AnimAdapter;

/**
 * Created by neiderm on 12/21/17.
 */

public class ModelComponent implements Component {

    private static int instcnt = 0;

    public final ModelInstance modelInst;
    public final float boundingRadius;
    public final Vector3 center = new Vector3();
    public final int id;

    public AnimAdapter animAdapter;
    public boolean isShadowed = true;


    public ModelComponent(ModelInstance instance) {

        Vector3 dimensions = new Vector3();

        this.id = ++instcnt;
        this.modelInst = instance;

        BoundingBox boundingBox = new BoundingBox();
//        boundingBox = instance.calculateBoundingBox(new BoundingBox());
        instance.calculateBoundingBox(boundingBox);
        boundingBox.getDimensions(dimensions);
        boundingBox.getCenter(center);
        boundingRadius = dimensions.len() / 2.0f;
    }
}
