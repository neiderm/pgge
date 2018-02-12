package com.mygdx.game;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

/**
 * Created by mango on 2/11/18.
 */

public class PhysicsPIDcontrol extends PIDcontrol {

    private btRigidBody body;
    private Vector3 tmpV = new Vector3(0, 0, 0);


    public PhysicsPIDcontrol(btRigidBody body, Vector3 setpoint, float kP, float kI, float kD) {

        super(setpoint, kP, kI, kD);
        this.body = body;
    }

    @Override
    public void doControl(Matrix4 transform) {

        translation = transform.getTranslation(translation);

        output = super.doControl(translation);

// now take output vector and apply as a force
        body.applyCentralForce(output.cpy().scl(0.1f));


        float setpY = setpoint.y + 2.0f; // copied height offset from playerSystem:updateChaseNode
        float myY = translation.y;

        if (myY < setpY){

            tmpV.set(0, (setpY - myY) * 0.2f, 0);
            // also, apply a force vector opposing  gravity
            body.applyCentralForce(tmpV);
        }
    }
}
