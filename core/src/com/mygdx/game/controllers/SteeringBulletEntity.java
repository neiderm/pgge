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


    private InputStruct io = new InputStruct();
    private Vector2 touchPadCoords = new Vector2();


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
     *
     */

/// this is just a hack right now ... it ain't right
// need to pass        steering.angular
        this.ctrl.inputSet(io.set(touchPadCoords.set(steering.linear.x, steering.linear.z), InputStruct.ButtonsEnum.BUTTON_NONE));

        this.ctrl.update(time);
    }


    private static final Matrix4 tmpMatrix4 = new Matrix4();
    private final Vector3 tmpVector3 = new Vector3();

    @Override
    public Vector3 getPosition () {
        body.getMotionState().getWorldTransform(tmpMatrix4);
        return tmpMatrix4.getTranslation(tmpVector3);
    }


    @Override
    public void setMaxLinearSpeed (float maxLinearSpeed) {
        this.maxLinearSpeed = maxLinearSpeed;
    }

    @Override
    public float getMaxLinearAcceleration () {
        return maxLinearAcceleration;
    }

    @Override
    public void setMaxLinearAcceleration (float maxLinearAcceleration) {
        this.maxLinearAcceleration = maxLinearAcceleration;
    }
}
