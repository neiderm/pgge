package com.mygdx.game.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by mango on 12/21/17.
 */

public class ModelComponent implements Component {

    public ModelInstance modelInst;
    public Vector3 scale;

    public ModelComponent(Model model, Matrix4 transform) {

        this.modelInst = new ModelInstance(model);

        // must link to the passed arg, it must be linked to bullet motion state
        this.modelInst.transform = transform;
    }

    public ModelComponent(Model model, Matrix4 transform, Vector3 scale) {

//        this.modelInst = new ModelInstance(model);

        // must link to the passed arg, it must be linked to bullet motion state
//        this.modelInst.transform = transform;

        this(model, transform);

        this.scale = new Vector3(scale);

    }
}
