package com.mygdx.game.controllers;

import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.screens.GameScreen;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;

import java.util.Random;

import static java.lang.Math.abs;


/**
 * Created by mango on 2/10/18.
 */

public class TankController extends ICharacterControlManual {

    private Vector2 inpVect = new Vector2(0, 0); // control input vector

    private btRigidBody body;
    private float mass;
    private BulletWorld world;

    public TankController(btRigidBody body, float mass) {
        this.body = body;
        this.mass = mass;
        this.world = BulletWorld.getInstance();
    }

    // working variables
    private Matrix4 tmpM = new Matrix4();
    private Vector3 tmpV = new Vector3();
    private Vector3 down = new Vector3();
    private Quaternion rotation = new Quaternion();


    private final Vector3 linearForceV = new Vector3();
    private final Vector3 angularForceV = new Vector3();
    private final Vector3 impulseForceV = new Vector3();


    @Override
    public void inputSet(Object ioObject) {

        InputStruct io = (InputStruct) ioObject;
        inpVect.set(io.inpVector);

        calcSteeringOutput(inpVect);


        InputStruct.ButtonsEnum button = io.buttonPress;

        switch (button) {
            case BUTTON_A:
                break;
            case BUTTON_B:
                break;
            case BUTTON_C:
                applyJump();
                break;
            default:
                break;
        }
    }


    // magnitude of force applied (property of "vehicle" type?)
    static final float FORCE_MAG = 12.0f;


    /*
     * from bulletSteeringUtils
     */
    public static float vectorToAngle(Vector3 vector) {
// return (float)Math.atan2(vector.z, vector.x);
        return (float) Math.atan2(-vector.z, vector.x);
    }


    // tmp?
    @Override
    public void calcSteeringOutput(SteeringAcceleration<Vector3> steering) {

        // idfk
        if (true)//if (0 == steering.angular)
            calcSteeringOutput_private(steering.linear);
        else
            calcSteeringOutput_private(steering);
    }


    Vector3 tmpVector3 = new Vector3();



    /*
     *  https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/bullet/SteeringBulletEntity.java
     *   protected void applySteering (SteeringAcceleration<Vector3> steering, float deltaTime)
     */
    private void calcSteeringOutput_private(SteeringAcceleration<Vector3> steeringOutput) {
///*
        boolean anyAccelerations = false;

        // Update position and linear velocity
        if (!steeringOutput.linear.isZero()) {

            float forceMult = FORCE_MAG * this.mass; // fudge factor .. enemy has too much force!
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
                body.applyTorque(tmpVector3.set(0, steeringOutput.angular, 0));
                anyAccelerations = true;
            }
        }
        else {
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
            float maxLinearSpeed = getMaxLinearSpeed();
            if (currentSpeedSquare > maxLinearSpeed * maxLinearSpeed) {
                body.setLinearVelocity(velocity.scl(maxLinearSpeed / (float)Math.sqrt(currentSpeedSquare)));
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


    private void calcSteeringOutput_private(Vector3 linear) {

        // angular force not used with Seek behavior
//        angularForceV.set(0, angular * 5.0f, 0);  /// degrees multiplier is arbitrary!

        // need our present position and rotation no matter what
        body.getWorldTransform(tmpM);
        tmpM.getTranslation(tmpV);
        tmpM.getRotation(rotation);

        // have to take my position and take linearforce as relatve, sum them vectors and pass that as center
        Ray ray = new Ray(); // tank direction
        Vector3 forward = new Vector3();

        if (null != body)
            ModelInstanceEx.rotateRad(forward.set(0, 0, -1), body.getOrientation());
        else
            body = null; // wtf

        ray.set(tmpV, forward);

        float len = ray.direction.dot(linear.x, 0, linear.z);
        Vector3 adjForceVect = new Vector3();
        adjForceVect.set(ray.direction.x * len, 0, ray.direction.z * len);

//        float forceMult = 10.0f * FORCE_MAG * 0.75f; // fudge factor .. enemy has too much force!
        float forceMult = FORCE_MAG * this.mass; // fudge factor .. enemy has too much force!
// hmm ...
        linearForceV.set(adjForceVect);
        linearForceV.scl(forceMult); //
// ha idea is sound but NFW right now so just do something
/*
        linearForceV.set(linear);
        linearForceV.scl(forceMult * this.mass);
*/

// next we want delta of commanded liniear force V vs. actual and the proportionately apply rotation force
        float bodyYaw = rotation.getYawRad();
        float forceYaw = vectorToAngle(linearForceV);
        float error = forceYaw - bodyYaw;

        float gain = 0.0f;  // arbitrary gain?
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


    public void calcSteeringOutput(Vector2 inpVect) {

        final float DZ = 0.25f; // actual number is irrelevant if < deadzoneRadius of TouchPad

        // rotate by a constant rate according to stick left or stick right.
        float degrees = 0;
        if (inpVect.x < -DZ) {
            degrees = 1;
        } else if (inpVect.x > DZ) {
            degrees = -1;
        }

        if (null != body)
            ModelInstanceEx.rotateRad(linearForceV.set(0, 0, -1), body.getOrientation());
        else
            body = null; // wtf

        if (inpVect.y > DZ) {
            // reverse thrust & "steer" opposite direction !
            linearForceV.scl(-1);
            degrees *= -1;
        } else if (!(inpVect.y < -DZ)) {
            linearForceV.set(0, 0, 0);
        }


        linearForceV.scl(FORCE_MAG * this.mass);

        angularForceV.set(0, degrees * 5.0f, 0);  /// degrees multiplier is arbitrary!
    }


    private Random rnd = new Random();

    void applyJump() {
        // random flip left or right
        if (rnd.nextFloat() > 0.5f)
            tmpV.set(0.1f, 0, 0);
        else
            tmpV.set(-0.1f, 0, 0);

        body.applyImpulse(impulseForceV.set(0, rnd.nextFloat() * 10.f + 40.0f, 0), tmpV);
    }


    @Override
    public void update(float delta) {

        // check for contact w/ surface, only apply force if in contact, not falling
        // 1 meters max from the origin seems to work pretty good
        body.getWorldTransform(tmpM);
        tmpM.getTranslation(tmpV);

        if (null != body)
            ModelInstanceEx.rotateRad(down.set(0, -1, 0), body.getOrientation());
        else
            body = null; // wtf

        RenderSystem.otherThings.add(GfxUtil.line(tmpV,
                ModelInstanceEx.rotateRad(down.set(0, -1, 0), tmpM.getRotation(rotation)),
                Color.RED));


        btCollisionObject rayPickObject = world.rayTest(tmpV, down, 1.0f);

        if (null != rayPickObject) {
            updateControl(delta);
        }
    }


    private void updateControl(float delta) {

        body.applyTorque(angularForceV);

// eventually we should take time into account not assume 16mS?
    /* somehow the friction application is working out so well that no other limit needs to be
     imposed on the veloocity ... sometime will try to formalize the math! */

        /* kinetic friction? ... ground/landscape is not dynamic and doesn't provide friction!
         * ultimately, somehow MU needs to be a property of the "surface" player is contact with and
         * passed as parameter to the friction computation .
         * Somehow, this seems to work well - the vehicle accelerates only to a point at which the
         * velocity seems to be limited and constant ... go look up the math eventually */
        final float MU = 0.5f;

        body.applyCentralForce(linearForceV);

        body.applyCentralForce(body.getLinearVelocity().scl(-MU * this.mass));

        body.setWorldTransform(tmpM);
    }



//    https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/bullet/BulletSteeringTest.java

    private boolean independentFacing;
    private float maxLinearSpeed;
    private float maxAngularSpeed;


    public boolean isIndependentFacing () {
        return independentFacing;
    }
// from steeringBulletEntity
    public void setIndependentFacing (boolean independentFacing) {
        this.independentFacing = independentFacing;
    }

//    @Override
    public Vector3 getLinearVelocity () {
        return body.getLinearVelocity();
    }

//    @Override
    public float getMaxLinearSpeed () {
        return maxLinearSpeed;
    }

//    @Override
    public float getMaxAngularSpeed () {
        return maxAngularSpeed;
    }

//    @Override
    public float getZeroLinearSpeedThreshold () {
        return 0.001f;
    }
}
