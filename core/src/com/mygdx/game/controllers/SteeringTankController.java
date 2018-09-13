package com.mygdx.game.controllers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.BlendedSteering;
import com.badlogic.gdx.ai.steer.behaviors.LookWhereYouAreGoing;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.steer.limiters.NullLimiter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.util.ModelInstanceEx;

import static java.lang.Math.abs;


/**
 * Created by mango on 2/10/18.
 * ref:
 *  https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/bullet/BulletSteeringTest.java
 *  https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#the-steering-system-api
 *  https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/bullet/tests/BulletSeekTest.java
 */

public class SteeringTankController extends TankController {

    public SteeringTankController(Entity copyEntity, boolean independentFacing, SteeringBulletEntity target) {

        this(copyEntity.getComponent(BulletComponent.class).body,
                copyEntity.getComponent(BulletComponent.class).mass);


// TODO: toss all this crap in Steering Tank Controller !!!!
        setMaxLinearSpeed(2); // idfk
        setMaxLinearAcceleration(1 /* 200 */); // GN: idfk

        final Seek<Vector3> seekSB = new Seek<Vector3>(this, target);
//        character.setSteeringBehavior(seekSB);

        setMaxLinearAcceleration(500);
        setMaxLinearSpeed(5);
        setMaxAngularAcceleration(50);
        setMaxAngularSpeed(10);

        setMaxLinearAcceleration(1);
        setMaxLinearSpeed(2);
        setMaxAngularAcceleration(10);
        setMaxAngularSpeed(10);

        final LookWhereYouAreGoing<Vector3> lookWhereYouAreGoingSB = new LookWhereYouAreGoing<Vector3>(this) //
                .setAlignTolerance(.005f) //
                .setDecelerationRadius(MathUtils.PI) //
                .setTimeToTarget(.1f);

        Arrive<Vector3> arriveSB = new Arrive<Vector3>(this, target) //
                .setTimeToTarget(0.1f) // 0.1f
                .setArrivalTolerance(0.2f) // 0.0002f
                .setDecelerationRadius(3);

        BlendedSteering<Vector3> blendedSteering = new BlendedSteering<Vector3>(this) //
                .setLimiter(NullLimiter.NEUTRAL_LIMITER) //
                .add(arriveSB, 1f) //
                .add(lookWhereYouAreGoingSB, 1f);

        setSteeringBehavior(blendedSteering);
    }

    public SteeringTankController(btRigidBody body, float mass) {

        super(body);
        this.mass = mass;
        this.world = BulletWorld.getInstance();
    }

    // working variables
    private Matrix4 tmpM = new Matrix4();
    private Vector3 tmpV = new Vector3();
    private Quaternion rotation = new Quaternion();
    private Vector3 adjForceVect = new Vector3();
    private Vector3 forward = new Vector3();


    @Override
    protected void applySteering(SteeringAcceleration<Vector3> steering, float delta) {

        // need our present position and rotation no matter what
        body.getWorldTransform(tmpM);
        tmpM.getTranslation(tmpV);
        tmpM.getRotation(rotation);

        // have to take my position and take linearforce as relatve, sum them vectors and pass that as center
        Ray ray = new Ray(); // tank direction

        if (null != body)
            ModelInstanceEx.rotateRad(forward.set(0, 0, -1), body.getOrientation());
        else
            body = null; // wtf

        ray.set(tmpV, forward);

        float len = ray.direction.dot(steering.linear.x, 0, steering.linear.z);
        adjForceVect.set(ray.direction.x * len, 0, ray.direction.z * len); //adjForceVect.set(forward.x, 0, forward.z); // idfk

        steering.linear.set(adjForceVect);


// next we want delta of commanded linearF force V vs. actual and the proportionately apply rotation force
        float bodyYaw = rotation.getYawRad();
        float forceYaw = vectorToAngle(steering.linear);

        // there is no angular steering output genrated by seek behaviour, others may use it
        steering.angular = forceYaw - bodyYaw; // = steeringOutput.angular;//idfk

        final float deadband = 0.1f; // whatever

        if (abs(steering.angular) < deadband) {
            steering.angular = 0f;
        }

        super.applySteering(steering, delta);
    }
}
