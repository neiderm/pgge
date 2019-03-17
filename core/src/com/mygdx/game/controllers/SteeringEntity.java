
package com.mygdx.game.controllers;

import com.badlogic.gdx.ai.steer.SteerableAdapter;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector3;

/**
 * copied from 
 *  https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/bullet/SteeringBulletEntity.java
 * (@author Daniel Holderbaum)
 */
public class SteeringEntity extends SteerableAdapter<Vector3> {

    private SteeringBehavior<Vector3> steeringBehavior;
    private final SteeringAcceleration<Vector3> steeringOutput = new SteeringAcceleration<Vector3>(new Vector3());

    public SteeringEntity(){ /* empty */ }


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

            // Apply steering acceleration (why is apply steering not in the interface???)
            applySteering(steeringOutput, deltaTime);
        }
    }

    protected void applySteering(SteeringAcceleration<Vector3> steering, float deltaTime) {
/* empty */
    }
}
