package com.mygdx.game.screens;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;

/**
 * Created by mango on 12/18/17.
 */

public class physObj {

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

    public MotionState motionstate;

    public btRigidBody body;
    public static btDynamicsWorld collisionWorld;


    public physObj(Vector3 sz, float mass, ModelInstance modelInst, btCollisionShape shape) {

        Vector3 tmp = new Vector3();

        if (mass == 0) {
            modelInst.transform.scl(sz);
            tmp = Vector3.Zero.cpy();
            motionstate = null;
        } else {
            shape.calculateLocalInertia(mass, tmp);
            motionstate = new MotionState(modelInst.transform);
        }

        btRigidBody.btRigidBodyConstructionInfo bodyInfo ;
//        bodyInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, motionstate, shape, tmp);
                bodyInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, motionstate, shape, tmp.cpy());
        body = new btRigidBody(bodyInfo);
        body.setFriction(0.8f);

        bodyInfo.dispose();

        if (mass == 0) {
            body.translate(tmp.set(modelInst.transform.val[12], modelInst.transform.val[13], modelInst.transform.val[14]));
        }

        collisionWorld.addRigidBody(body);
    }

    public void dispose() {
        body.dispose();
//        shape.dispose();
//        bodyInfo.dispose();
//		motionstate.dispose();  body deletion does this?
    }
}
