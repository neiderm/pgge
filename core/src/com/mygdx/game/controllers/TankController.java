package com.mygdx.game.controllers;

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


/**
 * Created by mango on 2/10/18.
 * <p>
 * TODO: rename this "SimpleVehicleModel" or something ... leading to
 * e.g. "trackedDifferentialVehicleModel" whatever
 */
public class TankController implements SimpleVehicleModel
{
    private static final float LINEAR_GAIN = 12.0f; // magnitude of force applied (property of "vehicle" type?)
    private static final float ANGULAR_GAIN = 5.0f; // degrees multiplier is arbitrary!;

    protected btRigidBody body;
    private BulletWorld world;
    protected float mass;

    // working variables
    private Vector3 trans = new Vector3();
    private Matrix4 tmpM = new Matrix4();
    private Vector3 tmpV = new Vector3();
    private Vector3 down = new Vector3();
    private Quaternion rotation = new Quaternion();



    public TankController(btRigidBody body, float mass) {

        this.body = body;
        this.mass = mass;
        this.world = BulletWorld.getInstance();
    }


    private Vector3 linear = new Vector3();
    private GfxUtil gfxLine = new GfxUtil();

    @Override
    public void updateControls(float direction, float angular, float time) {

        // this makes reverse steering opposite of my favorite *rigs game ;)
        if (direction < 0)
            angular *= -1; // reverse thrust & "steer" opposite direction !


        // TODO: logic to test orientation for "upside down but not free falling"

        // Determine resultant pushing force by rotating the linear direction vector (0, 0, 1 or 0, 0, -1) to
        // the body orientation, Vechicle steering uses resultant X & Y components of steeringLinear to apply
        // a pushing force to the vehicle along tt's Z axis. This gets a desired effect of i.e. magnitude
        // of applied force reduces proportionately the more that the vehicle is on an incline
        ModelInstanceEx.rotateRad(linear.set(0, 0, direction), body.getOrientation());


        // check for contact w/ surface, only apply force if in contact, not falling
        // 1 meters max from the origin seems to work pretty good
        body.getWorldTransform(tmpM);
        tmpM.getTranslation(trans);

        ModelInstanceEx.rotateRad(down.set(0, -1, 0), body.getOrientation());

        btCollisionObject rayPickObject = world.rayTest(trans, down, 1.0f);

        if (null != rayPickObject) {
            /*
             * apply forces only jump if in surface conttact
             */
            body.applyTorque(tmpV.set(0, angular * ANGULAR_GAIN, 0));

// eventually we should take time into account not assume 16mS?
    /* somehow the friction application is working out so well that no other limit needs to be
     imposed on the veloocity ... sometime will try to formalize the math! */

            /* kinetic friction? ... ground/landscape is not dynamic and doesn't provide friction!
             * ultimately, somehow MU needs to be a property of the "surface" player is contact with and
             * passed as parameter to the friction computation .
             * Somehow, this seems to work well - the vehicle accelerates only to a point at which the
             * velocity seems to be limited and constant ... go look up the math eventually */
            final float MU = 0.5f;

            linear.scl(LINEAR_GAIN * this.mass);

            body.applyCentralForce(linear);
            body.applyCentralForce(body.getLinearVelocity().scl(-MU * this.mass));
            body.setWorldTransform(tmpM);
        }

        RenderSystem.debugGraphics.add(gfxLine.line(trans,
                ModelInstanceEx.rotateRad(down.set(0, -1, 0), tmpM.getRotation(rotation)),
                Color.RED));

//Gdx.app.log(this.getClass().getName(), String.format("GfxUtil.line x = %f y = %f, z = %f", trans.x, trans. y, trans.z));
    }
}
