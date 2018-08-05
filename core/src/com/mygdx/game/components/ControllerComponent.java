package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;
import com.mygdx.game.controllers.ICharacterControlAuto;

/**
 * Created by mango on 2/10/18.
 */

/*
  instantiate with instance of any suitable simple controller e.g. PID controller etc.
 */
public class ControllerComponent implements Component {

    public ICharacterControlAuto controller;

    // temporary?
//    public Matrix4 setpoint;
//    public Matrix4 process;
    public Matrix4 transform;

    public ControllerComponent(){/* nothing to see yere */}

    public ControllerComponent(Matrix4 transform ){

        this.transform = transform;
    }

    public ControllerComponent(ICharacterControlAuto controller) {
        this.controller = controller;
    }

/*
    public ControllerComponent(ICharacterControlAuto controller, Matrix4 setpoint, Matrix4 process) {
        this(controller);
        this.setpoint = setpoint;
        this.process = process;
    }
    */
}
