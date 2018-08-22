package com.mygdx.game.controllers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.steer.SteerableAdapter;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.components.BulletComponent;


/* Ref:
 *  https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/bullet/SteeringBulletEntity.java
 */

public class SteeringBulletEntity extends SteerableAdapter<Vector3> {

    private btRigidBody body;

    private float maxLinearSpeed;
    private float maxLinearAcceleration;

    private SteeringBehavior<Vector3> steeringBehavior;
    private static final SteeringAcceleration<Vector3> steeringOutput =
            new SteeringAcceleration<Vector3>(new Vector3());

    private ICharacterControlAuto ctrl;

    private boolean independentFacing;
    private static final Vector3 ANGULAR_LOCK = new Vector3(0, 1, 0);


    public SteeringBulletEntity(
            Entity copyEntity, ICharacterControlAuto ctrl, boolean independentFacing) {

        this(copyEntity, ctrl);

        body.setAngularFactor(ANGULAR_LOCK);

        this.independentFacing = independentFacing;// not actually used
//        (TankController)ctrl.setIndependentFacing(true);
    }

    public SteeringBulletEntity(Entity copyEntity, ICharacterControlAuto ctrl) {

        this.body = copyEntity.getComponent(BulletComponent.class).body;
        this.ctrl = ctrl;
    }


    public SteeringBehavior<Vector3> getSteeringBehavior() {
        return steeringBehavior;
    }

    public void setSteeringBehavior(SteeringBehavior<Vector3> steeringBehavior) {
        this.steeringBehavior = steeringBehavior;
    }


    public void update(float deltaTime) {

        if (steeringBehavior != null) {
            // Calculate steering acceleration
            steeringBehavior.calculateSteering(steeringOutput);

            /*
             * Here you might want to add a motor control layer filtering steering accelerations.
             *
             * For instance, a car in a driving game has physical constraints on its movement:
             * - it cannot turn while stationary
             * - the faster it moves, the slower it can turn (without going into a skid)
             * - it can brake much more quickly than it can accelerate
             * - it only moves in the direction it is facing (ignoring power slides)
             */

            // Apply steering acceleration to move this agent
            applySteering(steeringOutput, deltaTime);
        }
    }


    private void applySteering(SteeringAcceleration<Vector3> steering, float time) {
/*
GN: this is where I call the TankController:update()
     *
     * steering output is a 2d vector applied to the controller ...
     *     ctrlr.inputSet(touchPadCoords.set(t.getKnobPercentX(), -t.getKnobPercentY()), buttonStateFlags)
     *
     *     ... controller applies as a force vector aligned parallel w/ body Z axis,
     *     and then simple rotates the body 1deg/16mS about the Y axis if there is any left/right component to the touchpad.
     *
     *     Simply throw away Y component of steering output?
     */
        this.ctrl.calcSteeringOutput(steering);

        this.ctrl.update(time);
    }


    private static final Matrix4 tmpMatrix4 = new Matrix4();
    private final Vector3 tmpVector3 = new Vector3();


    @Override
    public Vector3 getLinearVelocity() {
        return body.getLinearVelocity();
    }


    @Override
    public Vector3 getPosition() {
        body.getMotionState().getWorldTransform(tmpMatrix4);
        return tmpMatrix4.getTranslation(tmpVector3);
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
    public float getMaxLinearSpeed() {
        return maxLinearSpeed;
    }

    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed) {
        this.maxLinearSpeed = maxLinearSpeed;
    }


    /*
     * from bulletSteeringUtils
     */
    @Override
    public float vectorToAngle(Vector3 vector) {
//        return BulletSteeringUtils.vectorToAngle(vector);
        // return (float)Math.atan2(vector.z, vector.x);
        return (float) Math.atan2(-vector.z, vector.x);
    }


    // getMaxAngularSpeed

}
