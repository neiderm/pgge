/*
 * Copyright (c) 2021-2022 Glenn Neidermeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
public class PhysicsComponent implements Component {

/*
 * todo: override equals methos in this class
 */
    public static class MotionState extends btMotionState {

        public final Matrix4 transform;

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

    public final btCollisionShape shape; // this reference apparently is available thru the bc body
    public final float mass;
    public btRigidBody body;
    public MotionState motionstate;

    public PhysicsComponent(btCollisionShape shape, Matrix4 transform, float mass) {

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
