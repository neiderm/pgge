package com.mygdx.game;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.Components.BulletComponent;

/**
 * Created by mango on 2/10/18.
 */

public class SliderForceControl {


    // magnitude of force applied (property of "vehicle" type?)
    private static /* final */ float forceMag = 12.0f;

    /* kinetic friction? ... ground/landscape is not dynamic and doesn't provide friction!
     * ultimately, somehow MU needs to be a property of the "surface" player is contact with and
     * passed as parameter to the friction computation .
     * Somehow, this seems to work well - the vehicle accelerates only to a point at which the
     * velocity seems to be limited and constant ... go look up the math eventually */
    private static final float MU = 0.5f;

    //    private Engine engine;
    private BulletComponent bc;

    // working variables
    private static Matrix4 tmpM = new Matrix4();
    private static Vector3 tmpV = new Vector3();

    public /* private */ static final Vector3 forceVect = new Vector3(); // allowed this to be seen for debug info

    private Vector3 axis = new Vector3();


    public void update(float delta, Vector2 inpVect, Vector3 objectDownVector, BulletWorld www) {

        final float DZ = 0.25f; // actual number is irrelevant if < deadzoneRadius of TouchPad

        // rotate by a constant rate according to stick left or stick right.
        float degrees = 0;
        if (inpVect.x < -DZ) {
            degrees = 1;
        } else if (inpVect.x > DZ) {
            degrees = -1;
        }

        Quaternion r = bc.body.getOrientation();

        forceVect.set(0, 0, -1);
        float rad = r.getAxisAngleRad(axis);
        forceVect.rotateRad(axis, rad);


        if (inpVect.y > DZ) {
            // reverse thrust & "steer" opposite direction !
            forceVect.scl(-1);
            degrees *= -1;
        } else if (!(inpVect.y < -DZ)) {
            forceVect.set(0, 0, 0);
        }


        // check for contact w/ surface, only apply force if in contact, not falling
        if (www.surfaceContact(bc.body.getOrientation(), tmpV, objectDownVector)) {

            // we should maybe be using torque for this to be consistent in dealing with our rigid body player!
            tmpM.rotate(0, 1, 0, degrees); // does not touch translation ;)

            SliderForceControl.comp(delta, // eventually we should take time into account not assume 16mS?
                    bc.body, forceVect, forceMag, MU, bc.mass);
        }
    }



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
