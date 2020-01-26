package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.mygdx.game.animations.AnimAdapter;

/**
 * Created by neiderm on 12/21/17.
 */

public class ModelComponent implements Component {

    public AnimAdapter animAdapter;

    public Model model; // reference to model for dynamically reloading/reconfiguring meshes
    public ModelInstance modelInst;
    public float boundingRadius;
    public boolean isShadowed = true;
    public Vector3 center = new Vector3();

    public int id;
    public int modelInfoIndx = -1;
    private static int instcnt = 0;

    // name of loading Gameobject, kludgily used for some things
    public String strObjectName;


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
