package com.mygdx.game.systems;

import com.badlogic.gdx.math.Vector3;

/**
 * Created by mango on 2/9/18.
 */

/*
  a bit of a misnomer, this is not truly generic since the process variable and
  setpoints are 3d vectors ... could it be done with generics?
 */
public class PIDcontrol {

    private float kP = 0.1f;
    private float kI = 0;
    private float kD = 0;

    private Vector3 setpoint;

    PIDcontrol(float kP, float kI, float kD) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
    }

    PIDcontrol(Vector3 setpoint, float kP, float kI, float kD) {

        this(kP, kI, kD);
        this.setpoint = setpoint;
    }


    // working variables
    private static Vector3 output = new Vector3();
    private static Vector3 error = new Vector3();


    public Vector3 doControl(Vector3 processVariable) {

        return doControl(this.setpoint, processVariable);
    }


    public Vector3 doControl(Vector3 setpoint, Vector3 processVariable) {

        // e = setpoint - process .... process is current value of whatever we're controlling
        error = setpoint.sub(processVariable);

//        float kI = 0.001f;
//        integral.add(error.cpy().scl(delta * kI));

//        float kP = 0.1f;
        output.set(error.cpy().scl(kP)); // proportional
//output.add(integral);

        return output;
    }
}

