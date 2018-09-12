package com.mygdx.game.characters;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.BlendedSteering;
import com.badlogic.gdx.ai.steer.behaviors.LookWhereYouAreGoing;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.steer.limiters.NullLimiter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.controllers.SteeringBulletEntity;
import com.mygdx.game.controllers.SteeringTankController;

/*
 * ref:
 *   https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#the-steering-system-api
 *   https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/bullet/tests/BulletSeekTest.java
 */
public class EnemyCharacter extends SteeringTankController {

    public EnemyCharacter(Entity enemyTank, SteeringBulletEntity target ){

        super(enemyTank, false);

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
}
