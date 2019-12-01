package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.mygdx.game.BulletWorld;

/**
 * Created by neiderm on 12/21/2017.
 */

public class BulletComponent implements Component {


    public static class MotionState extends btMotionState {

        public Matrix4 transform;

        MotionState(final Matrix4 transform) {
            this.transform = transform;
        }

        @Override
        public void getWorldTransform(Matrix4 worldTrans) {
            worldTrans.set(transform);
        }

        @Override
        public void setWorldTransform(Matrix4 worldTrans) {
            transform.set(worldTrans);
        }
    }

    public MotionState motionstate;
    public btCollisionShape shape;  // this refefrence actually avialable thru the bc body
    public btRigidBody body;
    public float mass;


    public BulletComponent(btCollisionShape shape, Matrix4 transform, float mass) {

        this.shape = shape;
        this.mass = mass;

        Vector3 localInertia = new Vector3();

        if (mass != 0) {
            this.shape.calculateLocalInertia(mass, localInertia);
            motionstate = new MotionState(transform);
        }

        btRigidBody.btRigidBodyConstructionInfo bodyInfo =
                new btRigidBody.btRigidBodyConstructionInfo(mass, motionstate, this.shape, localInertia);

        this.body = new btRigidBody(bodyInfo);
//float crap = this.body.getFriction(); 0.5 default
        this.body.setFriction(0.8f); // doesn't make a difference for static/kinematic objects?

        bodyInfo.dispose();

        this.body.setWorldTransform(transform);


        BulletWorld.getInstance().addBody(this.body);
    }
}
