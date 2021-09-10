/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.mygdx.game.controllers;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;

/**
 * copied from
 * https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/bullet/SteeringBulletEntity.java
 * (@author Daniel Holderbaum)
 */
public class SteeringBulletEntity extends SteeringEntity {

    protected btRigidBody body;

    private float maxLinearSpeed;
    private float maxLinearAcceleration;
    private float maxAngularSpeed;
    private float maxAngularAcceleration;
    private boolean tagged;
    private boolean independentFacing;
    /* GN: in base class
    protected SteeringBehavior<Vector3> steeringBehavior;
    private static final SteeringAcceleration<Vector3> steeringOutput = new SteeringAcceleration<Vector3>(new Vector3());
    */
    private final Matrix4 tmpMatrix4 = new Matrix4();
    private final Vector3 tmpVector3 = new Vector3();

    // GN:
    SteeringBulletEntity() {
    }

    public SteeringBulletEntity(btRigidBody body) {
        this.body = body;
    }

/* GN: not used
	public SteeringBulletEntity (BulletEntity copyEntity, boolean independentFacing) {
		super(copyEntity.modelInstance, copyEntity.body);

		if (!(copyEntity.body instanceof btRigidBody)) {
			throw new IllegalArgumentException("Body must be a rigid body.");
		}
// if (copyEntity.body.isStaticOrKinematicObject()) {
// throw new IllegalArgumentException("Body must be a dynamic body.");
// }

		this.body = (btRigidBody)copyEntity.body;
		body.setAngularFactor(ANGULAR_LOCK);
		
		this.independentFacing = independentFacing; 
	}
*/
/* GN: base class
    public SteeringBehavior<Vector3> getSteeringBehavior() {
        return steeringBehavior;
    }

    public void setSteeringBehavior(SteeringBehavior<Vector3> steeringBehavior) {
        this.steeringBehavior = steeringBehavior;
    }
*/
//	float oldOrientation = 0;

/* GN: in base class
    public void update(float deltaTime) {
        if (steeringBehavior != null) {
            // Calculate steering acceleration
            steeringBehavior.calculateSteering(steeringOutput);

            *//*
     * Here you might want to add a motor control layer filtering steering accelerations.
     *
     * For instance, a car in a driving game has physical constraints on its movement: it cannot turn while stationary; the
     * faster it moves, the slower it can turn (without going into a skid); it can brake much more quickly than it can
     * accelerate; and it only moves in the direction it is facing (ignoring power slides).
     *//*

            // Apply steering acceleration
            applySteering(steeringOutput, deltaTime);
        }
    }
    */

/*
    @Override // GN:
    protected void applySteering(SteeringAcceleration<Vector3> steering, float deltaTime) {
        boolean anyAccelerations = false;

        // Update position and linear velocity
        if (!steeringOutput.linear.isZero()) {
            // this method internally scales the force by deltaTime
            body.applyCentralForce(steeringOutput.linear);
            anyAccelerations = true;
        }

        // Update orientation and angular velocity
        if (isIndependentFacing()) {
            if (steeringOutput.angular != 0) {
                // this method internally scales the torque by deltaTime
                body.applyTorque(tmpVector3.set(0, steeringOutput.angular, 0));
                anyAccelerations = true;
            }
        } else {
            // If we haven't got any velocity, then we can do nothing.
            Vector3 linVel = getLinearVelocity();
            if (!linVel.isZero(getZeroLinearSpeedThreshold())) {
                //
                // TODO: Commented out!!!
                // Looks like the code below creates troubles in combination with the applyCentralForce above
                // Maybe we should be more consistent by only applying forces or setting velocities.
                //
//				float newOrientation = vectorToAngle(linVel);
//				Vector3 angVel = body.getAngularVelocity();
//				angVel.y = (newOrientation - oldOrientation) % MathUtils.PI2;
//				if (angVel.y > MathUtils.PI) angVel.y -= MathUtils.PI2;
//				angVel.y /= deltaTime;
//				body.setAngularVelocity(angVel);
//				anyAccelerations = true;
//				oldOrientation = newOrientation;
            }
        }
        if (anyAccelerations) {
            body.activate();

            // TODO:
            // Looks like truncating speeds here after applying forces doesn't work as expected.
            // We should likely cap speeds form inside an InternalTickCallback, see
            // http://www.bulletphysics.org/mediawiki-1.5.8/index.php/Simulation_Tick_Callbacks

            // Cap the linear speed
            Vector3 velocity = body.getLinearVelocity();
            float currentSpeedSquare = velocity.len2();
            float _maxLinearSpeed = getMaxLinearSpeed();
            if (currentSpeedSquare > _maxLinearSpeed * _maxLinearSpeed) {
                body.setLinearVelocity(velocity.scl(_maxLinearSpeed / (float) Math.sqrt(currentSpeedSquare)));
            }

            // Cap the angular speed
            Vector3 angVelocity = body.getAngularVelocity();
            if (angVelocity.y > getMaxAngularSpeed()) {
                angVelocity.y = getMaxAngularSpeed();
                body.setAngularVelocity(angVelocity);
            }
        }
    }
*/

    public boolean isIndependentFacing() {
        return independentFacing;
    }

    public void setIndependentFacing(boolean independentFacing) {
        this.independentFacing = independentFacing;
    }

/* GN:? where was transform defined?
	@Override
	public void setOrientation (float orientation) {
		transform.setToRotationRad(0, 1, 0, orientation);
		body.setWorldTransform(transform);
	}

	@Override
	public float getOrientation () {
		transform.getRotation(tmpQuaternion, true);
		return tmpQuaternion.getYawRad();
	}
*/

    @Override
    public Vector3 getLinearVelocity() {
        return body.getLinearVelocity();
    }

    @Override
    public float getAngularVelocity() {
        Vector3 angularVelocity = body.getAngularVelocity();
        return angularVelocity.y;
    }

    @Override
    public float getBoundingRadius() {
        // TODO: this should be calculated via the actual btShape
        return 0.5f;
    }

    @Override
    public boolean isTagged() {
        return tagged;
    }

    @Override
    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    /* GN: ?
        @Override
        public Location<Vector3> newLocation () {
            return new BulletLocation();
        }
    */
    @Override
    public float vectorToAngle(Vector3 vector) {
//		return BulletSteeringUtils.vectorToAngle(vector);
        return (float) Math.atan2(-vector.z, vector.x); // GN: why negative ?
    }

    /* GN: ?
        @Override
        public Vector3 angleToVector (Vector3 outVector, float angle) {
            return BulletSteeringUtils.angleToVector(outVector, angle);
        }
    */
    @Override
    public Vector3 getPosition() {

        btMotionState ms = body.getMotionState();

        if (null != ms) { // careful ... body reference may not be null, but the linked entity may have been destroyed
            body.getMotionState().getWorldTransform(tmpMatrix4);
        } else {
            return new Vector3(); // idfk - target entity has probly been removed ... "arrive SB" has no provision to protect against null pointer
        }

        return tmpMatrix4.getTranslation(tmpVector3);
    }

    @Override
    public float getMaxLinearSpeed() {
        return maxLinearSpeed;
    }

    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed) {
        this.maxLinearSpeed = maxLinearSpeed;
    }

    @Override
    public float getMaxLinearAcceleration() {
        return maxLinearAcceleration;
    }

    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration) {
        this.maxLinearAcceleration = maxLinearAcceleration;
    }

    @Override
    public float getMaxAngularSpeed() {
        return maxAngularSpeed;
    }

    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {
        this.maxAngularSpeed = maxAngularSpeed;
    }

    @Override
    public float getMaxAngularAcceleration() {
        return maxAngularAcceleration;
    }

    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration) {
        this.maxAngularAcceleration = maxAngularAcceleration;
    }

    @Override
    public float getZeroLinearSpeedThreshold() {
        return 0.001f;
    }

    @Override
    public void setZeroLinearSpeedThreshold(float value) {
        throw new UnsupportedOperationException();
    }
}
