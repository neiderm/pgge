package com.mygdx.game.characters;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.BlendedSteering;
import com.badlogic.gdx.ai.steer.behaviors.LookWhereYouAreGoing;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.steer.limiters.NullLimiter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.controllers.SteeringBulletEntity;
import com.mygdx.game.controllers.SteeringTankController;


/*
 * ref:
 *   https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#the-steering-system-api
 *   https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/bullet/tests/BulletSeekTest.java
 */
public class EnemyCharacter implements IGameCharacter {

    private SteeringTankController character;

    public EnemyCharacter(Entity enemy, Entity player ){
        /*
          tmp: not going to set the controller comp on the entity for now ... enemy character will call controller methods directly
         */
        //e.add(new ControllerComponent(this.ctrl));

        CharacterComponent comp = new CharacterComponent(this, null /* tmp? */);
        enemy.add(comp);


        character = new SteeringTankController(enemy, false);
        character.setMaxLinearSpeed(2); // idfk
        character.setMaxLinearAcceleration(1 /* 200 */); // GN: idfk

        // use the base type here
        SteeringBulletEntity target = new SteeringBulletEntity(player);

        final Seek<Vector3> seekSB = new Seek<Vector3>(character, target);
//        character.setSteeringBehavior(seekSB);
///*
        character.setMaxLinearAcceleration(500);
        character.setMaxLinearSpeed(5);
        character.setMaxAngularAcceleration(50);
        character.setMaxAngularSpeed(10);

        character.setMaxLinearAcceleration(1);
        character.setMaxLinearSpeed(2);
        character.setMaxAngularAcceleration(10);
        character.setMaxAngularSpeed(10);

        final LookWhereYouAreGoing<Vector3> lookWhereYouAreGoingSB = new LookWhereYouAreGoing<Vector3>(character) //
                .setAlignTolerance(.005f) //
                .setDecelerationRadius(MathUtils.PI) //
                .setTimeToTarget(.1f);

        Arrive<Vector3> arriveSB = new Arrive<Vector3>(character, target) //
                .setTimeToTarget(0.1f) // 0.1f
                .setArrivalTolerance(0.2f) // 0.0002f
                .setDecelerationRadius(3);

        BlendedSteering<Vector3> blendedSteering = new BlendedSteering<Vector3>(character) //
                .setLimiter(NullLimiter.NEUTRAL_LIMITER) //
                .add(arriveSB, 1f) //
                .add(lookWhereYouAreGoingSB, 1f);

        character.setSteeringBehavior(blendedSteering);
//*/
    }


    @Override
    public void update(Entity entity, float deltaTime, Object whatever /* comp.lookRay */) {

        this.character.update(deltaTime);
    }
}
