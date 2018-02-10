package com.mygdx.game.systems;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

/**
 * Created by mango on 2/10/18.
 */

public class SliderForceControl {

    /* somehow the friction application is working out so well that no other limit needs to be
     imposed on the veloocity ... sometime will try to formalize the math!
      */
    public static void comp(float delta, // eventually we should take time into account not assume 16mS?
                            btRigidBody body,
                            Vector3 vForce, // direction of force (unit vector)
                            float mForce, // magnitude of force
                            float mu, // positive coef friction (we will apply negated value)
                            float mass
    ) {
        body.applyCentralForce(vForce.cpy().scl(mForce * mass));

        body.applyCentralForce(body.getLinearVelocity().scl(-mu * mass));

    }

}
