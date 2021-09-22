/*
 * Copyright (c) 2021 Glenn Neidermeier
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
package com.mygdx.game.controllers;

import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.BlendedSteering;
import com.badlogic.gdx.ai.steer.behaviors.LookWhereYouAreGoing;
import com.badlogic.gdx.ai.steer.limiters.NullLimiter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.screens.InputMapper;

import static java.lang.Math.abs;

/**
 * Created by neiderm on 2/10/18.
 * ref:
 * https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/bullet/BulletSteeringTest.java
 * https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#the-steering-system-api
 * https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/bullet/tests/BulletSeekTest.java
 */
public class SteeringTankController extends SteeringBulletEntity {

    private final CharacterController tc;

    public SteeringTankController(CharacterController tc, btRigidBody body, SteeringEntity target) {

        super(body);
        this.tc = tc;

        setMaxLinearAcceleration(1);
        setMaxLinearSpeed(2);
        setMaxAngularAcceleration(10);
        setMaxAngularSpeed(10);

        final LookWhereYouAreGoing<Vector3> lookWhereYouAreGoingSB = new LookWhereYouAreGoing<>(this)
                .setAlignTolerance(0.005f)
                .setDecelerationRadius(MathUtils.PI)
                .setTimeToTarget(0.1f);

        Arrive<Vector3> arriveSB = new Arrive<>(this, target)
                .setTimeToTarget(0.1f)
                .setArrivalTolerance(0.2f)
                .setDecelerationRadius(3);

        BlendedSteering<Vector3> blendedSteering = new BlendedSteering<>(this)
                .setLimiter(NullLimiter.NEUTRAL_LIMITER)
                .add(arriveSB, 1f)
                .add(lookWhereYouAreGoingSB, 1f);

        setSteeringBehavior(blendedSteering);
    }

    // working variables
    private final Matrix4 tmpM = new Matrix4();
    private final Quaternion rotation = new Quaternion();

    @Override
    protected void applySteering(SteeringAcceleration<Vector3> steering, float delta) {

        body.getWorldTransform(tmpM);
        tmpM.getRotation(rotation);

        // proportionately apply rotation force to steer the Rig
        float bodyYaw = rotation.getYawRad();
        float forceYaw = vectorToAngle(steering.linear);

        // there is no angular steering output generated by seek behavior
        float angular = forceYaw - bodyYaw;
        final float deadband = 0.1f;

        if (abs(angular) < deadband) {
            angular = 0f;
        }
        /*
           sync the Rig model with the virtual controller inputs
         */
        final float Forward = -1.0f; // forward (on z axis)
        InputMapper.ControlBundle cbundle = tc.getControlBundle();
        cbundle.analogX = angular;
        cbundle.analogY = Forward;
        cbundle.setCbuttonState(1, false);  // jump/brake
        tc.updateControls(0);
    }
}
