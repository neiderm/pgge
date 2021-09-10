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
import com.mygdx.game.screens.InputMapper;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;

public class TankController implements ControllerAbstraction {

    private static final float LINEAR_GAIN = 12.0f; // magnitude of force applied (property of "vehicle" type?)
    private static final float ANGULAR_GAIN = 5.0f; // degrees multiplier is arbitrary!

    protected final btRigidBody body;
    protected final float mass;

    // working variables
    private final Matrix4 tmpM = new Matrix4();
    private final Vector3 trans = new Vector3();
    private final Vector3 tmpV = new Vector3();
    private final Vector3 accelV = new Vector3();
    private final GfxUtil gfxLine = new GfxUtil();

    private boolean izinnaJump;

    public TankController(btRigidBody body, float mass) {
        this.body = body;
        this.mass = mass;
    }

    @Override
    public void updateControls(float[] analogs, boolean[] switches, float time) {

        float angular = (null != analogs) ? analogs[InputMapper.VIRTUAL_AD_AXIS] : 0;
        float direction = (null != analogs) ? analogs[InputMapper.VIRTUAL_WS_AXIS] : 0;

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
            if (!izinnaJump && (null != switches) && (switches[SW_FIRE2])) {
                final float ANGULAR_ROLL_GAIN = -0.2f; // note negate direction sign same in both forward and reverse
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
            float MU = 0.5f;
            // check brake switch
            if (null != switches && switches[SW_FIRE2]) {
                MU *= 9.0f; // emp-fudgically determined
            }

            // Determine resultant pushing force by rotating the accelV direction vector (0, 0, 1 or 0, 0, -1)
            // to the body orientation, resultant X & Y components of steeringLinear applied as a
            // pushing force to the rig along it's Z axis. This achieves a desired effect in that the magnitude
            // of applied force reduces proportionately the more that the rig is on an incline.
            ModelInstanceEx.rotateRad(accelV.set(0, 0, direction), orientation);

            accelV.scl(LINEAR_GAIN * this.mass);

            body.applyCentralForce(accelV);

            // Apply some "drag" to the Rig so that the forward velocity will (eventually) cap off at some reasonable limit
            body.applyCentralForce(body.getLinearVelocity().scl(-MU * this.mass)); // "friction"

            // controller implementation should treat as a pair of switches which are coupled together
            // each providing a "half" of the travel of a virtual slide axis
            float slide;
            // slide it the left ... negatively
            slide = (null != analogs) ? analogs[InputMapper.VIRTUAL_L2_AXIS] : 0;
            ModelInstanceEx.rotateRad(accelV.set((-slide), 0, 0), orientation);

            accelV.scl(LINEAR_GAIN * this.mass);
            body.applyCentralForce(accelV);

            // slide it the right
            slide = (null != analogs) ? analogs[InputMapper.VIRTUAL_R2_AXIS] : 0;
            ModelInstanceEx.rotateRad(accelV.set(slide, 0, 0), orientation);

            accelV.scl(LINEAR_GAIN * this.mass);
            body.applyCentralForce(accelV);

            // if touching the ground, and not starting a new jump, then the jump flag can be unlatched
            izinnaJump = false;
        }

// todo: if debug draw
        GfxBatch.draw(gfxLine.line(trans,
                ModelInstanceEx.rotateRad(tmpV.set(0, -1, 0), tmpM.getRotation(orientation)),
                Color.RED));
    }
}
