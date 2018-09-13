package com.mygdx.game.characters;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.BlendedSteering;
import com.badlogic.gdx.ai.steer.behaviors.LookWhereYouAreGoing;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.steer.limiters.NullLimiter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.controllers.SteeringBulletEntity;
import com.mygdx.game.controllers.SteeringTankController;

/*
 * ref:
 *   https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#the-steering-system-api
 *   https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/bullet/tests/BulletSeekTest.java
 */
public class EnemyCharacter extends SteeringTankController {

    public EnemyCharacter(Entity enemyTank, SteeringBulletEntity target ){

        super(enemyTank, false, target);
    }
}
