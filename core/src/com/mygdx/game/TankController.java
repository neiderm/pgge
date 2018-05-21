package com.mygdx.game;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

import static com.mygdx.game.util.ModelInstanceEx.rotateV;

/**
 * Created by mango on 2/10/18.
 */

public class TankController /* extends CharacterController  */ {

    private TankController(){
    }

    // working variables
    private static Matrix4 tmpM = new Matrix4();

    public /* private */ static final Vector3 forceVect = new Vector3(); // allowed this to be seen for debug info


    public static void update(btRigidBody body, float mass, float delta, Vector2 inpVect) {

        final float DZ = 0.25f; // actual number is irrelevant if < deadzoneRadius of TouchPad

        // rotate by a constant rate according to stick left or stick right.
        float degrees = 0;
        if (inpVect.x < -DZ) {
            degrees = 1;
        } else if (inpVect.x > DZ) {
            degrees = -1;
        }

        rotateV(forceVect.set(0, 0, -1), body.getOrientation());

        if (inpVect.y > DZ) {
            // reverse thrust & "steer" opposite direction !
            forceVect.scl(-1);
            degrees *= -1;
        } else if (!(inpVect.y < -DZ)) {
            forceVect.set(0, 0, 0);
        }

        body.getWorldTransform(tmpM);
        // we should maybe be using torque for this to be consistent in dealing with our rigid body player!
        tmpM.rotate(0, 1, 0, degrees); // does not touch translation ;)

// eventually we should take time into account not assume 16mS?
    /* somehow the friction application is working out so well that no other limit needs to be
     imposed on the veloocity ... sometime will try to formalize the math! */

        // magnitude of force applied (property of "vehicle" type?)
        final float forceMag = 12.0f;
        /* kinetic friction? ... ground/landscape is not dynamic and doesn't provide friction!
         * ultimately, somehow MU needs to be a property of the "surface" player is contact with and
         * passed as parameter to the friction computation .
         * Somehow, this seems to work well - the vehicle accelerates only to a point at which the
         * velocity seems to be limited and constant ... go look up the math eventually */
        final float MU = 0.5f;

        body.applyCentralForce(forceVect.cpy().scl(forceMag * mass));

        body.applyCentralForce(body.getLinearVelocity().scl(-MU * mass));

        body.setWorldTransform(tmpM);
    }
}
