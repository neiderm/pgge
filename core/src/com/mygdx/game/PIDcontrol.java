package com.mygdx.game;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import static com.badlogic.gdx.math.MathUtils.cos;
import static com.badlogic.gdx.math.MathUtils.sin;

/**
 * Created by mango on 2/9/18.
 */

/*
  a bit of a misnomer, this is not truly generic since the process variable and
  setpoints are 3d vectors ... could it be done with generics?
 */
public class PIDcontrol /* implements Character */ {

    private float kP = 0.1f;
    private float kI = 0;
    private float kD = 0;

    protected Matrix4 process; // reference to present value of whatever we're controlling
    protected Matrix4 setpoint; // this will probably usually be a position we're trying to reach
    protected Vector3 spOffset;  // offset for camera chaser, or 0 for direct target

    PIDcontrol(){
    }

    private PIDcontrol(float kP, float kI, float kD) {
        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
    }

    public PIDcontrol(Matrix4 setpoint,
                      Matrix4 process,
                      Vector3 spOffset, float kP, float kI, float kD) {

        this(kP, kI, kD);

// this one is reference to the position we're tracking, which could be changing
        this.setpoint = setpoint;
        this.spOffset = new Vector3(spOffset);
        this.process = process;
    }


    // working variables
    protected static Vector3 output = new Vector3();
    protected Vector3 error = new Vector3();
    protected Vector3 translation = new Vector3();

    private static Vector3 vec3 = new Vector3();

//    @Override
    public void update(float delta) {

        Matrix4 currentPositionTransform = this.process;

        translation = currentPositionTransform.getTranslation(vec3);
        translation.add(doControl(translation));
        currentPositionTransform.setTranslation(translation);
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
    private static Vector3 tmpV = new Vector3();
    /*
     * target position vector is updated in place on the passed reference.
     * identity matrix is set to the transform and returned.

This should become just a general case of vector addition (offset added to the position reference we're tracking)
     */
    public Matrix4 addSetpointOffset(Vector3 targetPosition, Matrix4 actorTransform) {

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
}
