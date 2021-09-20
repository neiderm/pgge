/*
 * Copyright (c) 2021 Glenn Neidermeier
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mygdx.game.controllers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.screens.GfxBatch;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;

public class TankController extends ControllerAdapter {

    // experimental values should probably be a property of the Rig type
    private static final float LINEAR_GAIN = 12.0f; // forward driving force
    private static final float ANGULAR_GAIN = 5.0f; // steering force

    protected final btRigidBody body;
    protected final float mass;

    // working variables
    private final Matrix4 tmpM = new Matrix4();
    private final Vector3 trans = new Vector3();
    private final Vector3 tmpV = new Vector3();
    private final Vector3 accelV = new Vector3();
    private final GfxUtil gfxLine = new GfxUtil();

    private boolean izinnaJump;

    public TankController(btRigidBody body, float mass, ControlBundle controlBundle) {
        this.body = body;
        this.mass = mass;
        // allow the default Control Bundle in the Adapter to be left to Garbage Collection
        if (null != controlBundle) {
            this.controlBundle = controlBundle;
        }
    }

    public TankController(btRigidBody body, float mass) {
        this(body, mass, null);
    }

    @Override
    public void updateControls(float time) {

        float angular = controlBundle.analogX;
        float direction = controlBundle.analogY;
        float slideLeft = controlBundle.analogL;
        float slideRight = controlBundle.analogR;
        boolean swBrakeJump = controlBundle.switch1;

        // this makes reverse steering opposite of my favorite *rigs game ;)
        if (direction < 0) {
            angular *= -1.0f; // reverse thrust & "steer" opposite direction !
        }
        // TODO: logic to test orientation for "upside down but not free falling"

        // check for contact w/ surface, only apply force if in contact, not falling
        // 1 meters max from the origin seems to work pretty good
        body.getWorldTransform(tmpM);
        tmpM.getTranslation(trans);

        Quaternion orientation = body.getOrientation();

        // set a unit-vector as negative Y axis, then rotate that to rig body orientation to get the ray
        tmpV.set(0, -1, 0);

        ModelInstanceEx.rotateRad(tmpV.set(0, -1, 0), orientation);

        btCollisionObject rayPickObject = BulletWorld.getInstance().rayTest(trans, tmpV, 1.0f);

        // roll-over function on Fire2 / B button
        if (null == rayPickObject /* || angle > xxxx */) {
            if (!izinnaJump && swBrakeJump) {
                final float ANGULAR_ROLL_GAIN = -0.2f;
                izinnaJump = true;
                ModelInstanceEx.rotateRad(
                        tmpV.set(angular * ANGULAR_ROLL_GAIN, 0.5f, 0), body.getOrientation());
                body.applyImpulse(accelV.set(0, 40.0f, 0), tmpV);
            }
        } else {
            /*
             * apply forces only if in surface contact
             */
            body.applyTorque(tmpV.set(0, angular * ANGULAR_GAIN, 0));
            /*
             * kinetic friction? ... ground/landscape is not dynamic and doesn't provide friction!
             * ultimately, somehow MU needs to be a property of the "surface" player is contact with and
             * passed as parameter to the friction computation. Works OK but would be nice to formalize the math.
             */
            float coefFriction = 0.5f;
            // check brake switch
            if (swBrakeJump) {
                coefFriction *= 9.0f; // emp-fudgically determined
            }
            // Determine resultant pushing force by rotating the accelV direction vector (0, 0, 1 or
            // 0, 0, -1) to the body orientation, resultant X & Y components of steeringLinear applied
            // as a pushing force to the Rig along it's Z axis. This achieves the desired effect that the
            // magnitude of applied force reduces in proportion to the degree of inclination of the Rig.
            ModelInstanceEx.rotateRad(accelV.set(0, 0, direction), orientation);

            accelV.scl(LINEAR_GAIN * mass);
            body.applyCentralForce(accelV);
            // the degree of friction applied should allow sufficient speed but keep maximum forward
            // velocity under some reasonable limit
            body.applyCentralForce(body.getLinearVelocity().scl(-coefFriction * mass)); // "friction"

            // left/right slide input
            float slide;
            // slide it the left ... negatively
            slide = slideLeft;
            ModelInstanceEx.rotateRad(accelV.set((-slide), 0, 0), orientation);
            accelV.scl(LINEAR_GAIN * mass);
            body.applyCentralForce(accelV);

            // slide it the right
            slide = slideRight;
            ModelInstanceEx.rotateRad(accelV.set(slide, 0, 0), orientation);
            accelV.scl(LINEAR_GAIN * mass);
            body.applyCentralForce(accelV);

            // if touching the ground, and not starting a new jump, then the jump flag can be unlatched
            izinnaJump = false;
        }

// tmp: line indicator on Z axix
        GfxBatch.draw(gfxLine.line(trans,
                ModelInstanceEx.rotateRad(tmpV.set(0, -1, 0), tmpM.getRotation(orientation)),
                Color.RED));
    }
}
