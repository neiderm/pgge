package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;

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

    public final btCollisionShape shape;
    public final btRigidBody body;
    public float mass;
//    private final int id;
//    private static int cnt = 0;


    public BulletComponent(btCollisionShape shape, Matrix4 transform, float mass) {

//        this.id = cnt++;
        this.shape = shape;
        this.mass = mass;

        Vector3 localInertia = new Vector3();
        MotionState motionstate = null;

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
    }

/*    public BulletComponent(btCollisionShape shape, Matrix4 transform) {

        this.id = cnt++;

        this.motionstate = new MotionState(transform);
        this.body = new btRigidBody(0, this.motionstate, shape);
        this.shape = shape;
    }*/
}
