package com.mygdx.game.characters;

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
 * {@code PlayerInput} behavior .... allows player input to be treated as Steerable
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
    protected Vector3 translation = new Vector3();

    private static Vector3 vec3 = new Vector3();

    private Vector3 tmpV = new Vector3();

    /*
     * implements a real simple proportional controller (for non-physics entity and eventually should
     * not be limited to any visual (i.e. model instance) entity
      */
    @Override
    protected SteeringAcceleration<T> calculateRealSteering(SteeringAcceleration<T> steering) {


        Matrix4 currentPositionTransform = this.process;
        translation = currentPositionTransform.getTranslation(vec3);
        translation.add(doControl(translation));
        currentPositionTransform.setTranslation(translation);


        steering.angular = 0f;

        // Output steering acceleration .. .whatever

        Vector3 steeringLinear = (Vector3)steering.linear;
        steeringLinear.set(tmpV.x, 0, tmpV.z); // casting help ;)

        return steering;
    }


    private Vector3 doControl(Vector3 currentPosition) {

        Vector3 adjTgtPosition = new Vector3();

        addSetpointOffset(adjTgtPosition, this.setpoint);

        // e = setpoint - process .... process is current value of whatever we're controlling
        error = adjTgtPosition.sub(currentPosition);

//        float kI = 0.001f;
//        integral.add(error.cpy().scl(delta * kI));

//        float kP = 0.1f;
        output.set(error.cpy().scl(kP)); // proportional
//output.add(integral);

        return output;
    }


    private static Quaternion quat = new Quaternion();
    private static Matrix4 transform = new Matrix4();


/*
 * target position vector is updated in place on the passed reference.
 * identity matrix is set to the transform and returned.
 *
 * This should become just a general case of vector addition (offset added to the position reference we're tracking)
 */
    private Matrix4 addSetpointOffset(Vector3 targetPosition, Matrix4 actorTransform) {

//        Vector3 targetPosition = new Vector3();

        targetPosition.set(actorTransform.getTranslation(tmpV));

        /* LATEST: this is screwy but workable. SEems more efficient if we take actor orientation
        vector, negative of that, then scale it by desired offset, and add 3d offset vector actor position
         ... for now just add z and y of offset vector

         Furthermore, it should track to the body actual dirction of movement, not it's orientation!
         */

        float height = this.spOffset.y;
        float dist = this.spOffset.z;

        // offset to maintain position above subject ...
        targetPosition.y += height;

        // ... and then determine a point slightly "behind"
        // take negative of unit vector of players orientation
        actorTransform.getRotation(quat);

        // hackme ! this is not truly in 3D!
        float yaw = quat.getYawRad();

        float dX = sin(yaw);
        float dZ = cos(yaw);
        targetPosition.x += dX * dist;
        targetPosition.z += dZ * dist;

        // idfk
        transform.setToTranslation(targetPosition);
        return transform;
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
