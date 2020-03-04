/*
 * Copyright (c) 2019 Glenn Neidermeier
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

public class TankController implements SimpleVehicleModel
{
//    // these will be inputmapper VIRTUAL_N_AXIS defines/enumeration whatever
//    public enum InputChannels {
//        AD_AXIS,
//        WS_AXIS,
//        X1_AXIS,
//        Y1_AXIS,
//        L2_AXIS, // virtual axis for front button "left 2" (if used)
//        R2_AXIS, // virtual axis for front button "right 2" (if used)
//    }

    private static final float LINEAR_GAIN = 12.0f; // magnitude of force applied (property of "vehicle" type?)
    private static final float ANGULAR_GAIN = 5.0f; // degrees multiplier is arbitrary!;

    protected btRigidBody body;
    protected float mass;

    // working variables
    private Matrix4 tmpM = new Matrix4();
    private Vector3 trans = new Vector3();
    private Vector3 tmpV = new Vector3();
    private Vector3 accelV = new Vector3();
    private GfxUtil gfxLine = new GfxUtil();


    public TankController(btRigidBody body, float mass) {

        this.body = body;
        this.mass = mass;
    }


    @Override
    public void updateControls(float[] analogs, boolean[] switches, float time) {

        float angular = (null != analogs) ?  analogs[InputMapper.VIRTUAL_AD_AXIS] : 0;
        float direction = (null != analogs) ?  analogs[InputMapper.VIRTUAL_WS_AXIS] : 0;

        boolean jump = (null != switches) &&  switches[0];

        final float ANGULAR_ROLL_GAIN = -0.2f; // note negate direction sign same in both forward and reverse

        if (jump) {         // cool jump!
            ModelInstanceEx.rotateRad(tmpV.set(angular * ANGULAR_ROLL_GAIN, 0.5f, 0), body.getOrientation());
            body.applyImpulse(accelV.set(0, 40.0f, 0), tmpV);
        }

        // this makes reverse steering opposite of my favorite *rigs game ;)
        if (direction < 0)
            angular *= -1; // reverse thrust & "steer" opposite direction !


        // TODO: logic to test orientation for "upside down but not free falling"


        // check for contact w/ surface, only apply force if in contact, not falling
        // 1 meters max from the origin seems to work pretty good
        body.getWorldTransform(tmpM);
        tmpM.getTranslation(trans);

        Quaternion orientation = body.getOrientation();
        ModelInstanceEx.rotateRad(tmpV.set(0, -1, 0), orientation);

        btCollisionObject rayPickObject = BulletWorld.getInstance().rayTest(trans, tmpV, 1.0f);

        if (null != rayPickObject) {
            /*
             * apply forces only if in surface conttact
             */
            body.applyTorque(tmpV.set(0, angular * ANGULAR_GAIN, 0));

// eventually we should take time into account not assume 16mS?
    /* somehow the friction application is working out so well that no other limit needs to be
     imposed on the veloocity ... sometime will try to formalize the math! */

            /* kinetic friction? ... ground/landscape is not dynamic and doesn't provide friction!
             * ultimately, somehow MU needs to be a property of the "surface" player is contact with and
             * passed as parameter to the friction computation .
             * Somehow, this seems to work well - the vehicle accelerates only to a point at which the
             * velocity seems to be limited and constant ... go look up the math eventually */
            final float MU = 0.5f;

            // Determine resultant pushing force by rotating the accelV direction vector (0, 0, 1 or 0, 0, -1) to
            // the body orientation, Vechicle steering uses resultant X & Y components of steeringLinear to apply
            // a pushing force to the vehicle along tt's Z axis. This gets a desired effect of i.e. magnitude
            // of applied force reduces proportionately the more that the vehicle is on an incline
            ModelInstanceEx.rotateRad(accelV.set(0, 0, direction), orientation);

            accelV.scl(LINEAR_GAIN * this.mass);
            body.applyCentralForce(accelV);
            body.applyCentralForce(body.getLinearVelocity().scl(-MU * this.mass)); // "friction"


            // controller implementation should treat as a pair of switches which are coupled together
            // each providing a "half" of the travel of a virtual slide axis
            float slide;
            //slide it the left ... negatively
            slide = (null != analogs) ? analogs[ InputMapper.VIRTUAL_L2_AXIS ] : 0;
            ModelInstanceEx.rotateRad(accelV.set( ( -slide), 0, 0), orientation);

            accelV.scl(LINEAR_GAIN * this.mass);
            body.applyCentralForce(accelV);

            //slide it the right
            slide = (null != analogs) ? analogs[ InputMapper.VIRTUAL_R2_AXIS ] : 0;
            ModelInstanceEx.rotateRad(accelV.set(slide, 0, 0), orientation);

            accelV.scl(LINEAR_GAIN * this.mass);
            body.applyCentralForce(accelV);

//            body.applyCentralForce(body.getLinearVelocity().scl(-MU * this.mass)); // friction?


            body.setWorldTransform(tmpM);
        }

        GfxBatch.draw(gfxLine.line(trans,
                ModelInstanceEx.rotateRad(tmpV.set(0, -1, 0), tmpM.getRotation(orientation)),
                Color.RED));
    }
}
