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

import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector3;

import static com.badlogic.gdx.math.MathUtils.cos;
import static com.badlogic.gdx.math.MathUtils.sin;

/**
 * {@code TrackerSB} behavior .... allows non-bullet entity to be treated as Steerable
 *
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * @author neiderm  (based on "com.badlogic.gdx.ai.steer.behaviors.Seek")
 */
public class TrackerSB<T extends Vector<T>> extends SteeringBehavior<T> {

    private float kP = 0.1f;
    private float kI = 0;
    private float kD = 0;

    private Matrix4 process; // reference to present value of whatever we're controlling
    private Matrix4 setpoint; // this will probably usually be a position we're trying to reach
    private Vector3 spOffset;  // offset for camera chaser, or 0 for direct target


    /**
     * Creates a {@code PlayerInput} behavior for the specified owner and target.
     *
     * @param owner the owner of this behavior
     */
    public TrackerSB(Steerable<T> owner,
              Matrix4 setpoint,
              Matrix4 process,
              Vector3 spOffset) {

        super(owner);

        this.setpoint = setpoint;
        this.spOffset = new Vector3(spOffset);
        this.process = process;
    }

    // working variables
    private Vector3 output = new Vector3();
    private Vector3 error = new Vector3();
    private  Vector3 translation = new Vector3();
    private Vector3 tmpV = new Vector3();
    private Vector3 adjTgtPosition = new Vector3();
    private static Quaternion quat = new Quaternion();

    /*
     * implements a real simple proportional controller (for non-physics entity and eventually should
     * not be limited to any visual (i.e. model instance) entity
      */
    @Override
    protected SteeringAcceleration<T> calculateRealSteering(SteeringAcceleration<T> steering) {

        Matrix4 currentPositionTransform = this.process;
        currentPositionTransform.getTranslation(translation);

        adjTgtPosition.set(this.setpoint.getTranslation(tmpV));

        /* LATEST: this is screwy but workable. SEems more efficient if we take actor orientation
        vector, negative of that, then scale it by desired offset, and add 3d offset vector actor position
         ... for now just add z and y of offset vector

         Furthermore, it should track to the body actual dirction of movement, not it's orientation!
         */

        float height = this.spOffset.y;
        float dist = this.spOffset.z;

        // offset to maintain position above subject ...
        adjTgtPosition.y += height;

        // ... and then determine a point slightly "behind"
        // take negative of unit vector of players orientation
        this.setpoint.getRotation(quat);

        // hackme ! this is not truly in 3D!
        float yaw = quat.getYawRad();

        float dX = sin(yaw);
        float dZ = cos(yaw);
        adjTgtPosition.x += dX * dist;
        adjTgtPosition.z += dZ * dist;

        // e = setpoint - process .... process is current value of whatever we're controlling
        Vector3 prevError = error;
        error = adjTgtPosition.sub(translation);
        // apply exponential filter (helps with "hunting" but probably still needs some limitation on the output)
        error.add(prevError).scl(0.5f); // error = (error + prevError) / 2;

//        float kI = 0.001f;
//        integral.add(error.cpy().scl(delta * kI));

//        float kP = 0.1f;
        output.set(error.scl(kP)); // proportional
        translation.add(output);

        currentPositionTransform.setTranslation(translation);

        steering.angular = 0f;

        // Output steering acceleration .. .whatever

        Vector3 steeringLinear = (Vector3)steering.linear;
        steeringLinear.set(tmpV.x, 0, tmpV.z); // casting help ;)

        return steering;
    }

    //
    // Setters overridden in order to fix the correct return type for chaining
    //

    @Override
    public TrackerSB<T> setOwner(Steerable<T> owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public TrackerSB<T> setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Sets the limiter of this steering behavior. The given limiter must at least take care of the maximum linear acceleration.
     *
     * @return this behavior for chaining.
     */
    @Override
    public TrackerSB<T> setLimiter(Limiter limiter) {
        this.limiter = limiter;
        return this;
    }
}
