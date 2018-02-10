package com.mygdx.game.systems;

import com.badlogic.gdx.math.Vector3;

/**
 * Created by mango on 2/9/18.
 */

public class PIDcontrol {

    float kP = 0.1f;
    float kI = 0;
    float kD = 0;


    PIDcontrol(float kP, float kI, float kD) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
    }

    private Vector3 output = new Vector3();
    private Vector3 error = new Vector3();

    public Vector3 doControl(Vector3 setpoint, Vector3 process) {

        // e = setpoint - process
        error = setpoint.sub(process);

//        float kI = 0.001f;
//        integral.add(error.cpy().scl(delta * kI));

//        float kP = 0.1f;
        output.set(error.cpy().scl(kP)); // proportional
//output.add(integral);

        return output;
    }
}

