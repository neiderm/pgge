package com.mygdx.game.controllers;

import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;

import java.util.Random;


/**
 * Created by mango on 2/10/18.
 */
public class TankController extends SteeringBulletEntity {

    static final float LINEAR_GAIN = 12.0f; // magnitude of force applied (property of "vehicle" type?)
    static final float ANGULAR_GAIN = 5.0f; // degrees multiplier is arbitrary!;


    // working variables
    private Vector3 trans = new Vector3();
    private Matrix4 tmpM = new Matrix4();
    private Vector3 tmpV = new Vector3();
    private Vector3 down = new Vector3();
    private Quaternion rotation = new Quaternion();


    TankController(){}

    TankController(btRigidBody body) {
        super(body);
    }

    public TankController(btRigidBody body, float mass) {

        this(body);
        this.mass = mass;
        this.world = BulletWorld.getInstance();
    }


    private Random rnd = new Random();
    private final Vector3 impulseForceV = new Vector3();

    private void applyJump() {
        // random flip left or right
        if (rnd.nextFloat() > 0.5f)
            tmpV.set(0.1f, 0, 0);
        else
            tmpV.set(-0.1f, 0, 0);

        body.applyImpulse(impulseForceV.set(0, rnd.nextFloat() * 10.f + 40.0f, 0), tmpV);
    }

    @Override
    protected void applySteering(SteeringAcceleration<Vector3> steering, float time) {

        if (steering.linear.y != 0) {
            applyJump(); // do this before scale linear ;)
            steering.linear.y = 0; // may or may not need zero this out
        }

        // check for contact w/ surface, only apply force if in contact, not falling
        // 1 meters max from the origin seems to work pretty good
        body.getWorldTransform(tmpM);
        tmpM.getTranslation(trans);

        ModelInstanceEx.rotateRad(down.set(0, -1, 0), body.getOrientation());

        btCollisionObject rayPickObject = world.rayTest(trans, down, 1.0f);

        if (null != rayPickObject) {

            body.applyTorque(tmpV.set(0, steering.angular * ANGULAR_GAIN, 0));

// eventually we should take time into account not assume 16mS?
    /* somehow the friction application is working out so well that no other limit needs to be
     imposed on the veloocity ... sometime will try to formalize the math! */

            /* kinetic friction? ... ground/landscape is not dynamic and doesn't provide friction!
             * ultimately, somehow MU needs to be a property of the "surface" player is contact with and
             * passed as parameter to the friction computation .
             * Somehow, this seems to work well - the vehicle accelerates only to a point at which the
             * velocity seems to be limited and constant ... go look up the math eventually */
            final float MU = 0.5f;

            steeringOutput.linear.scl(LINEAR_GAIN * this.mass);

            body.applyCentralForce(steeringOutput.linear);
            body.applyCentralForce(body.getLinearVelocity().scl(-MU * this.mass));
            body.setWorldTransform(tmpM);
        }

        RenderSystem.otherThings.add(GfxUtil.line(trans,
                ModelInstanceEx.rotateRad(down.set(0, -1, 0), tmpM.getRotation(rotation)),
                Color.RED));

//Gdx.app.log(this.getClass().getName(), String.format("GfxUtil.line x = %f y = %f, z = %f", trans.x, trans. y, trans.z));
    }
}

