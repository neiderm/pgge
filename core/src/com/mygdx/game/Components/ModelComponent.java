package com.mygdx.game.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;


/**
 * Created by mango on 12/21/17.
 */

public class ModelComponent implements Component {

    public ModelInstance modelInst;
    public Vector3 scale;
    public float boundingRadius = 0;
    public Vector3 center;
    public boolean isShadowed = true;


    /*
    public ModelComponent(Model model, Matrix4 transform, final String... rootNodeIds) {

        this.modelInst = new ModelInstance(model, transform, rootNodeIds);
    }
*/
    public ModelComponent(Model model, Matrix4 transform) {

        /*
         * note: ModelInstance will link it's instance member transform to the one we pass in ....
         * which is what we want, because we should also be linking it to the transform in the
         * bullet motion state!
         */
//        this.modelInst = new ModelInstance(model, transform);
        this(model, transform, null, null);

        // must link to the passed arg, it must be linked to bullet motion state i.e. ....
        //        this.modelInst.transform = transform;
    }

/*
    public ModelComponent(Model model, Matrix4 transform, Vector3 scale) {

        this(model, transform);
        this.scale = new Vector3(scale);
    }
*/


    public ModelComponent(Model model, Matrix4 transform, Vector3 scale, String rootNodeId) {

        // ?????? 	public ModelInstance (final Model model, final Matrix4 transform, final String... rootNodeIds) {
        if (null != rootNodeId)
            this.modelInst = new ModelInstance(model, transform, rootNodeId);
        else
            this.modelInst = new ModelInstance(model, transform);

        if (null != scale) {
            this.scale = new Vector3(scale);
        }
        BoundingBox boundingBox = new BoundingBox();
        center = new Vector3();
        Vector3 dimensions = new Vector3();
        this.modelInst.calculateBoundingBox(boundingBox);
        boundingBox.getCenter(center);
        boundingBox.getDimensions(dimensions);
        boundingRadius = dimensions.len() / 2f;
    }

    public ModelComponent(ModelInstance instance, Vector3 scale) {

        this.modelInst = instance;

        if (null != scale) {
            this.scale = new Vector3(scale);
        }
        BoundingBox boundingBox = new BoundingBox();
        center = new Vector3();
        Vector3 dimensions = new Vector3();
        this.modelInst.calculateBoundingBox(boundingBox);
        boundingBox.getCenter(center);
        boundingBox.getDimensions(dimensions);
        boundingRadius = dimensions.len() / 2f;
    }
}
