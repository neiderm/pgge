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
     * IN: transform
     *    caller passes their own transform so that it can be linked to the bullet component motion
     *    state transform
     */
    public ModelComponent(Model model, Matrix4 transform, Vector3 scale, String rootNodeId) {
        /* ??????
          thought I could get away with not checking rootNodeId==null here ... but alas not so.
         	public ModelInstance (final Model model, final Matrix4 transform, final String... rootNodeIds) {
        */
        if (null != rootNodeId)
            this.modelInst = new ModelInstance(model, transform, rootNodeId);
        else
            this.modelInst = new ModelInstance(model, transform, (String[])null);

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
