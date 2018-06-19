package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;


/**
 * Created by mango on 2/10/18.
 */

public class TankController implements CharacterController   {

    public Vector2 inpVect = new Vector2(0, 0); // control input vector

    private btRigidBody body;
    private float mass;
    private BulletWorld world;

    public TankController(BulletWorld world, btRigidBody body, float mass){
        this.body = body;
        this.mass = mass;
        this.world = world;
    }

    // working variables
    private static Matrix4 tmpM = new Matrix4();
    private static Vector3 tmpV = new Vector3();
    private static Vector3 down = new Vector3();
    private static Quaternion rotation = new Quaternion();

    public /* private */ static final Vector3 forceVect = new Vector3(); // allowed this to be seen for debug info

//    public Vector2 inpVect;


    public Vector2 getInputVector(){
        return this.inpVect;
    }


    @Override
    public void update(float delta) {

        // check for contact w/ surface, only apply force if in contact, not falling
        // 1 meters max from the origin seems to work pretty good
        body.getWorldTransform(tmpM);
        tmpM.getTranslation(tmpV);

        ModelInstanceEx.rotateRad(down.set(0, -1, 0), body.getOrientation());


        RenderSystem.otherThings.add(GfxUtil.line(tmpV,
                ModelInstanceEx.rotateRad(down.set(0, -1, 0), tmpM.getRotation(rotation)),
                Color.RED));


        btCollisionObject rayPickObject = world.rayTest(tmpV, down, 1.0f);

        if (null != rayPickObject) {
            updateControl(delta);
        }
    }

    private void updateControl(float delta){

        final float DZ = 0.25f; // actual number is irrelevant if < deadzoneRadius of TouchPad

        // rotate by a constant rate according to stick left or stick right.
        float degrees = 0;
        if (inpVect.x < -DZ) {
            degrees = 1;
        } else if (inpVect.x > DZ) {
            degrees = -1;
        }

        ModelInstanceEx.rotateRad(forceVect.set(0, 0, -1), body.getOrientation());

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

        body.applyCentralForce(forceVect.cpy().scl(forceMag * this.mass));

        body.applyCentralForce(body.getLinearVelocity().scl(-MU * this.mass));

        body.setWorldTransform(tmpM);
    }
}
