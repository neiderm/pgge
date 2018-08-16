package com.mygdx.game.controllers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;

import java.util.Random;

import static java.lang.Math.abs;


/**
 * Created by mango on 2/10/18.
 */

public class TankController extends ICharacterControlManual {

    private Vector2 inpVect = new Vector2(0, 0); // control input vector

    private btRigidBody body;
    private float mass;
    private BulletWorld world;

    public TankController(btRigidBody body, float mass) {
        this.body = body;
        this.mass = mass;
        this.world = BulletWorld.getInstance();
    }

    // working variables
    private Matrix4 tmpM = new Matrix4();
    private Vector3 tmpV = new Vector3();
    private Vector3 down = new Vector3();
    private Quaternion rotation = new Quaternion();


    private final Vector3 linearForceV = new Vector3();
    private final Vector3 angularForceV = new Vector3();
    private final Vector3 impulseForceV = new Vector3();


    @Override
    public void inputSet(Object ioObject) {

        InputStruct io = (InputStruct) ioObject;
        inpVect.set(io.inpVector);

        calcSteeringOutput(inpVect);


        InputStruct.ButtonsEnum button = io.buttonPress;

        switch (button) {
            case BUTTON_A:
                break;
            case BUTTON_B:
                break;
            case BUTTON_C:
                applyJump();
                break;
            default:
                break;
        }
    }


    // magnitude of force applied (property of "vehicle" type?)
    static final float FORCE_MAG = 12.0f;


    /*
     * from bulletSteeringUtils
     */
    public static float vectorToAngle(Vector3 vector) {
// return (float)Math.atan2(vector.z, vector.x);
        return (float) Math.atan2(-vector.z, vector.x);
    }

    // tmp?
@Override
    public void calcSteeringOutput(Vector3 linear, float angular) {

        final float adjForce = FORCE_MAG * 0.75f; // enemy has too much force!
        linearForceV.set(linear);
        linearForceV.scl(adjForce * this.mass); //


        // angular force not used with Seek behavior

//        angularForceV.set(0, angular * 5.0f, 0);  /// degrees multiplier is arbitrary!
        angularForceV.set(0, 0, 0);


        body.getWorldTransform(tmpM);

        tmpM.getRotation(rotation);


        float bodyYaw = rotation.getYawRad();
        float forceYaw = vectorToAngle(linearForceV);
        float error = bodyYaw - forceYaw;

        if (abs(error) > 0.1f) {
            error = error * 0.9f;
        }

        tmpM.rotate(0, 1, 0, error); // does not touch translation ;)
        body.setWorldTransform(tmpM);
    }


    public void calcSteeringOutput(Vector2 inpVect) {

        final float DZ = 0.25f; // actual number is irrelevant if < deadzoneRadius of TouchPad

        // rotate by a constant rate according to stick left or stick right.
        float degrees = 0;
        if (inpVect.x < -DZ) {
            degrees = 1;
        } else if (inpVect.x > DZ) {
            degrees = -1;
        }

        ModelInstanceEx.rotateRad(linearForceV.set(0, 0, -1), body.getOrientation());

        if (inpVect.y > DZ) {
            // reverse thrust & "steer" opposite direction !
            linearForceV.scl(-1);
            degrees *= -1;
        } else if (!(inpVect.y < -DZ)) {
            linearForceV.set(0, 0, 0);
        }


        linearForceV.scl(FORCE_MAG * this.mass);

        angularForceV.set(0, degrees * 5.0f, 0);  /// degrees multiplier is arbitrary!
    }


    private Random rnd = new Random();

    void applyJump() {
        // random flip left or right
        if (rnd.nextFloat() > 0.5f)
            tmpV.set(0.1f, 0, 0);
        else
            tmpV.set(-0.1f, 0, 0);

        body.applyImpulse(impulseForceV.set(0, rnd.nextFloat() * 10.f + 40.0f, 0), tmpV);
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


    private void updateControl(float delta) {


        body.applyTorque(angularForceV);

// eventually we should take time into account not assume 16mS?
    /* somehow the friction application is working out so well that no other limit needs to be
     imposed on the veloocity ... sometime will try to formalize the math! */

        /* kinetic friction? ... ground/landscape is not dynamic and doesn't provide friction!
         * ultimately, somehow MU needs to be a property of the "surface" player is contact with and
         * passed as parameter to the friction computation .
         * Somehow, this seems to work well - the vehicle accelerates only to a point at which the
         * velocity seems to be limited and constant ... go look up the math eventually */
        final float MU = 0.5f;

        body.applyCentralForce(linearForceV);

        body.applyCentralForce(body.getLinearVelocity().scl(-MU * this.mass));

        body.setWorldTransform(tmpM);
    }
}
