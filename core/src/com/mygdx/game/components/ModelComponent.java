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
    public Vector3 center = new Vector3(); // idfk
    public boolean isShadowed = true;
    private Vector3 dimensions = new Vector3();
    private BoundingBox boundingBox;
    public int id = 0;
    private static int instcnt = 0;


    public ModelComponent(ModelInstance instance, Vector3 scale) {

        this.id = ++instcnt;

        this.modelInst = instance;

        if (null != scale) {
// https://stackoverflow.com/questions/21827302/scaling-a-modelinstance-in-libgdx-3d-and-bullet-engine
            instance.nodes.get(0).scale.set(scale);
            instance.calculateTransforms();
        }

        boundingBox = instance.calculateBoundingBox(new BoundingBox());
        boundingBox.getDimensions(this.dimensions);
        boundingBox.getCenter(this.center);
        boundingRadius = this.dimensions.len() / 2f;
    }
}
