package com.mygdx.game.controllers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.steer.SteerableAdapter;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.components.BulletComponent;


/* Ref:
 *  https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/bullet/SteeringBulletEntity.java
 */

public class SteeringBulletEntity extends SteerableAdapter<Vector3> {

    BulletWorld world;
    SteeringBehavior<Vector3> steeringBehavior;

    protected btRigidBody body;
    protected float mass;

    private float maxLinearSpeed;
    private float maxLinearAcceleration;
    private boolean independentFacing;
    private static final Vector3 ANGULAR_LOCK = new Vector3(0, 1, 0);


    SteeringBulletEntity(btRigidBody body) {
        this.body = body;
    }

    public SteeringBulletEntity(
            Entity copyEntity, boolean independentFacing) {

        this(copyEntity);

        body.setAngularFactor(ANGULAR_LOCK);

        this.independentFacing = independentFacing;// not actually used
    }

    public SteeringBulletEntity(Entity copyEntity) {

        this.body = copyEntity.getComponent(BulletComponent.class).body;
    }



    public SteeringBehavior<Vector3> getSteeringBehavior() {
        return steeringBehavior;
    }


    public void setSteeringBehavior(SteeringBehavior<Vector3> steeringBehavior) {

        this.steeringBehavior = steeringBehavior;
    }


    public void update(float deltaTime) { /* empty */ }

    protected void applySteering(SteeringAcceleration<Vector3> steering, float time) { /* empty */ }



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

    private float maxAngularSpeed;

    public void setIndependentFacing(boolean independentFacing) {
        this.independentFacing = independentFacing;
    }

    boolean isIndependentFacing() {
        return independentFacing;
    }

    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {
        this.maxAngularSpeed = maxAngularSpeed;
    }

    @Override
    public float getMaxAngularSpeed() {
        return maxAngularSpeed;
    }

    @Override
    public float getZeroLinearSpeedThreshold() {
        return 0.001f;
    }
}
