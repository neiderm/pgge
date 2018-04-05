package com.mygdx.game.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;

/**
 * Created by utf1247 on 12/21/2017.
 */

public class BulletComponent implements Component {


    public static class MotionState extends btMotionState {

        public Matrix4 transform;

        public MotionState(final Matrix4 transform) {
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

    public final MotionState motionstate;
    public final btCollisionShape shape;
    public final btRigidBody body;

    public btCollisionWorld collisionWorld;

    public float mass;

    // idfk
public boolean sFlag = false; // need to make component for static entity
    public final int id;
    public static int cnt = 0;



    public BulletComponent(btCollisionShape shape, Matrix4 transform, float mass) {

        Vector3 tmp = new Vector3();

        this.id = cnt++;
        this.shape = shape;
        this.mass = mass;

        if (mass == 0) {
            tmp = Vector3.Zero.cpy(); // GN: beware of modifying Zero!
            this.motionstate = null;
        } else {
            this.shape.calculateLocalInertia(mass, tmp);
            this.motionstate = new MotionState(transform);
        }

        btRigidBody.btRigidBodyConstructionInfo bodyInfo =
                new btRigidBody.btRigidBodyConstructionInfo(mass, this.motionstate, this.shape, tmp);
        this.body = new btRigidBody(bodyInfo);
//float crap = this.body.getFriction(); 0.5 default
        this.body.setFriction(0.8f); // doesn't make a difference for static/kinematic objects?

        bodyInfo.dispose();
    }

    public BulletComponent(btCollisionShape shape, Matrix4 transform) {

        this.id = cnt++;

        this.motionstate = new MotionState(transform);
        this.body = new btRigidBody(0, this.motionstate, shape);
        this.shape = shape;
    }
}
