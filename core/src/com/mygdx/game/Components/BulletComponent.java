package com.mygdx.game.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
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

    public MotionState motionstate;

    public btCollisionShape shape;
    public btRigidBody body;

    public Vector3 scale; // tmp?
    public ModelInstance modelInst; // tmp?

    public BulletComponent(){

    }

    public BulletComponent(Matrix4 transform){

//        motionstate = new physObj.MotionState(transform);
    }

    public BulletComponent(btCollisionShape shape, ModelInstance modelInst, Matrix4 transform) {

        // put the landscape at an angle so stuff falls of it...
        motionstate =
                new MotionState(new Matrix4().idt().rotate(new Vector3(1, 0, 0), 20f));
//                new MotionState(transform);

        this.body = new btRigidBody(0, motionstate, shape);
        this.modelInst = modelInst;
        this.shape = shape;

        modelInst.transform = motionstate.transform;
    }




}
