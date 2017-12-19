package com.mygdx.game.screens;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;

import java.util.ArrayList;

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
    public static Vector3 tmp = new Vector3();

    //    public static ArrayList<physObj> physObjects = new ArrayList<>();
    public static ArrayList<physObj> physObjects = new ArrayList<physObj>(); // GN: Error:(41, 66) error: diamond operator is not supported in -source 1.6 (use -source 7 or higher to enable diamond operator)

    public enum pType {
        SPHERE, BOX
    };

    public static Model boxTemplateModel;
    public static Model ballTemplateModel;
    public ModelInstance modelInst;

    public btCollisionShape shape;
    public btRigidBody.btRigidBodyConstructionInfo bodyInfo;
    public btRigidBody body;
    public static btDynamicsWorld collisionWorld;
    public final Vector3 scale;


    public physObj(pType tp, Vector3 sz, float mass, final Matrix4 transform) {
        if (tp == pType.BOX) {
            shape = new btBoxShape(sz);
            modelInst = new ModelInstance(boxTemplateModel);
        }

        if (tp == pType.SPHERE) {
            sz.y = sz.x;
            sz.z = sz.x; // sphere must be symetrical!
            shape = new btSphereShape(sz.x);
            modelInst = new ModelInstance(ballTemplateModel);
        }

        modelInst.transform = transform.cpy();
        scale = sz.cpy();

        if (mass == 0) {
            modelInst.transform.scl(sz);
            tmp = Vector3.Zero;
            motionstate = null;
        } else {
            shape.calculateLocalInertia(mass, tmp);
            motionstate = new MotionState(modelInst.transform);
        }

        bodyInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, motionstate, shape, tmp.cpy());
        body = new btRigidBody(bodyInfo);
        body.setFriction(0.8f);

        if (mass == 0) {
            body.translate(tmp.set(modelInst.transform.val[12], modelInst.transform.val[13], modelInst.transform.val[14]));
        }

        collisionWorld.addRigidBody(body);

        physObjects.add(this);
    }

    public void dispose() {
        body.dispose();
        shape.dispose();
        bodyInfo.dispose();
//		motionstate.dispose();  body deletion does this?

        physObjects.remove(this);

    }
}
