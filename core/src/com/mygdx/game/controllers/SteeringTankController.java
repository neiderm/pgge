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
 *   https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/bullet/BulletSteeringTest.java
 */

public class SteeringTankController extends TankController /*SteeringBulletEntity */{

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


    @Override
    protected void applySteering(SteeringAcceleration<Vector3> steering, float time) {

        // idfk
        if (true)//if (0 == steering.angular)
            calcSteeringOutput_oldG(steering);
        else
            calcSteeringOutput_new(steering);

//super.applySteering(steering, time);  ??? need to factor out the equivalent of the base
//        class applySteering() which will then have the equivalent of updateControl() added in
//        since updateControl is already common to the base class anyway.
    }


    /*
     *  https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/bullet/SteeringBulletEntity.java
     *   protected void applySteering (SteeringAcceleration<Vector3> steering, float deltaTime)
     */
    private void calcSteeringOutput_new(SteeringAcceleration<Vector3> steeringOutput) {
///*
        boolean anyAccelerations = false;

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

    private void calcSteeringOutput_oldG(SteeringAcceleration<Vector3> steeringOutput) {

        Vector3 linear = steeringOutput.linear;

        // angular force not used with Seek behavior
//        angularForceV.set(0, angular * 5.0f, 0);  /// degrees multiplier is arbitrary!

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

        float len = ray.direction.dot(linear.x, 0, linear.z);
        adjForceVect.set(ray.direction.x * len, 0, ray.direction.z * len);
//adjForceVect.set(forward.x, 0, forward.z);
        float forceMult = LINEAR_GAIN * this.mass; // fudge factor .. enemy has too much force!
// hmm ...
        linearForceV.set(adjForceVect);
        linearForceV.scl(forceMult); //
// ha idea is sound but NFW right now so just do something
/*
        linearForceV.set(linearF);
        linearForceV.scl(forceMult * this.mass);
*/

// next we want delta of commanded linearF force V vs. actual and the proportionately apply rotation force
        float bodyYaw = rotation.getYawRad();
        float forceYaw = vectorToAngle(linearForceV);
        float error = forceYaw - bodyYaw;
        error = steeringOutput.angular;//idfk
        float gain = 4.0f;  // arbitrary gain?
        float deadband = 0.1f; // whatever
        if (abs(error) > deadband) {
            error *= gain;
        }

        angularForceV.set(0, error, 0);

        GameScreen.linearForceV.set(linearForceV);
        GameScreen.bodyYaw = bodyYaw;
        GameScreen.forceYaw = forceYaw;
        GameScreen.error = error;
    }
}
