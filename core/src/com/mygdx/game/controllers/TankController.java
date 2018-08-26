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

    private final SteeringAcceleration<Vector3> steeringOutput =
            new SteeringAcceleration<Vector3>(new Vector3());

    final Vector3 linearForceV = new Vector3();
    final Vector3 angularForceV = new Vector3();
    private final Vector3 impulseForceV = new Vector3();

    // working variables
    private Matrix4 tmpM = new Matrix4();
    private Vector3 tmpV = new Vector3();
    private Vector3 down = new Vector3();
    private Quaternion rotation = new Quaternion();


    TankController(btRigidBody body) {
        super(body);
    }

    public TankController(btRigidBody body, float mass) {

        this(body);
        this.mass = mass;
        this.world = BulletWorld.getInstance();
    }


    private Random rnd = new Random();

    private void applyJump() {
        // random flip left or right
        if (rnd.nextFloat() > 0.5f)
            tmpV.set(0.1f, 0, 0);
        else
            tmpV.set(-0.1f, 0, 0);

        body.applyImpulse(impulseForceV.set(0, rnd.nextFloat() * 10.f + 40.0f, 0), tmpV);
    }

    @Override
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

            updateControl(deltaTime); // TODO: merge into applySteering()
        }
    }

    @Override
    protected void applySteering(SteeringAcceleration<Vector3> steering, float time) {

        // linearF force to be applied strictly along the body Z-axis
        linearForceV.set(0, 0, steering.linear.z);

        if (null != body) {
            ModelInstanceEx.rotateRad(linearForceV, body.getOrientation());
        } else
            body = null; // wtf  8/22 still gettin these :(

        linearForceV.scl(LINEAR_GAIN * this.mass);

        angularForceV.set(0, steering.angular * ANGULAR_GAIN, 0);

        if (steering.linear.y != 0)
            applyJump();
    }

    @Override
    protected void updateControl(float delta) {

        // check for contact w/ surface, only apply force if in contact, not falling
        // 1 meters max from the origin seems to work pretty good
        body.getWorldTransform(tmpM);
        tmpM.getTranslation(tmpV);

        ModelInstanceEx.rotateRad(down.set(0, -1, 0), body.getOrientation());

        btCollisionObject rayPickObject = world.rayTest(tmpV, down, 1.0f);

        if (null != rayPickObject) {

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

        RenderSystem.otherThings.add(GfxUtil.line(tmpV,
                ModelInstanceEx.rotateRad(down.set(0, -1, 0), tmpM.getRotation(rotation)),
                Color.RED));
    }
}
