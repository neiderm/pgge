package com.mygdx.game.controllers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.screens.GameScreen;
import com.mygdx.game.util.ModelInstanceEx;

import static java.lang.Math.abs;


/**
 * Created by mango on 2/10/18.
 * ref:
 * https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/bullet/BulletSteeringTest.java
 */

public class SteeringTankController extends TankController {

    public SteeringTankController(Entity copyEntity, boolean independentFacing) {

        this(copyEntity.getComponent(BulletComponent.class).body,
                copyEntity.getComponent(BulletComponent.class).mass);
    }

    private SteeringTankController(btRigidBody body, float mass) {

        super(body);
        this.mass = mass;
        this.world = BulletWorld.getInstance();
    }

    // working variables
    private Matrix4 tmpM = new Matrix4();
    private Vector3 tmpV = new Vector3();
    private Quaternion rotation = new Quaternion();


    //    @Override
    protected void _applySteering(SteeringAcceleration<Vector3> steering, float time) {

        // idfk
        if (true)//if (0 == steering.angular)
            applySteering_oldG(steering);
        else
            applySteering_new(steering);
    }


    /*
     *  https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/bullet/SteeringBulletEntity.java
     *   protected void applySteering (SteeringAcceleration<Vector3> steering, float deltaTime)
     */
    private void applySteering_new(SteeringAcceleration<Vector3> steeringOutput) {
///*
        boolean anyAccelerations = false;
        Vector3 linearForceV = null;//tmp
        // Update position and linearF velocity
        if (!steeringOutput.linear.isZero()) {

            float forceMult = LINEAR_GAIN * this.mass; // fudge factor .. enemy has too much force!
            linearForceV.set(steeringOutput.linear);
            linearForceV.scl(forceMult); //

            // this method internally scales the force by deltaTime
            body.applyCentralForce(steeringOutput.linear);
            anyAccelerations = true;
        }

        // Update orientation and angular velocity
        if (isIndependentFacing()) {
            if (steeringOutput.angular != 0) {
                // this method internally scales the torque by deltaTime
                body.applyTorque(tmpV.set(0, steeringOutput.angular, 0));
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

            // Cap the linearF speed
            Vector3 velocity = body.getLinearVelocity();
            float currentSpeedSquare = velocity.len2();
            float maxLinearSpeed = getMaxLinearSpeed();
            if (currentSpeedSquare > maxLinearSpeed * maxLinearSpeed) {
                body.setLinearVelocity(velocity.scl(maxLinearSpeed / (float) Math.sqrt(currentSpeedSquare)));
            }

            // Cap the angular speed
            Vector3 angVelocity = body.getAngularVelocity();
            if (angVelocity.y > getMaxAngularSpeed()) {
                angVelocity.y = getMaxAngularSpeed();
                body.setAngularVelocity(angVelocity);
            }
        }
//*/
    }

    private Vector3 adjForceVect = new Vector3();
    private Vector3 forward = new Vector3();

    private void applySteering_oldG(SteeringAcceleration<Vector3> steeringOutput) {
    } // tmp

    @Override
    protected void applySteering(SteeringAcceleration<Vector3> steering, float delta) {

        // need our present position and rotation no matter what
        body.getWorldTransform(tmpM);
        tmpM.getTranslation(tmpV);
        tmpM.getRotation(rotation);

        // have to take my position and take linearforce as relatve, sum them vectors and pass that as center
        Ray ray = new Ray(); // tank direction

        if (null != body)
            ModelInstanceEx.rotateRad(forward.set(0, 0, -1), body.getOrientation());
        else
            body = null; // wtf

        ray.set(tmpV, forward);

        float len = ray.direction.dot(steering.linear.x, 0, steering.linear.z);
        adjForceVect.set(ray.direction.x * len, 0, ray.direction.z * len); //adjForceVect.set(forward.x, 0, forward.z); // idfk

        steering.linear.set(adjForceVect);


// next we want delta of commanded linearF force V vs. actual and the proportionately apply rotation force
        float bodyYaw = rotation.getYawRad();
        float forceYaw = vectorToAngle(steering.linear);

        // there is no angular steering output genrated by seek behaviour, others may use it
        steering.angular = forceYaw - bodyYaw; // = steeringOutput.angular;//idfk

        final float deadband = 0.1f; // whatever

        if (abs(steering.angular) < deadband) {
            steering.angular = 0f;
        }

        super.applySteering(steering, delta);

        GameScreen.linearForceV.set(steering.linear);
        GameScreen.bodyYaw = bodyYaw;
        GameScreen.forceYaw = forceYaw;
        GameScreen.error = steering.angular;
    }
}
