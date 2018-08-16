package com.mygdx.game.characters;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.controllers.ICharacterControlAuto;
import com.mygdx.game.controllers.SteeringBulletEntity;
import com.mygdx.game.controllers.TankController;


/*
 * ref:
 *   https://github.com/libgdx/gdx-ai/wiki/Steering-Behaviors#the-steering-system-api
 *   https://github.com/libgdx/gdx-ai/blob/master/tests/src/com/badlogic/gdx/ai/tests/steer/bullet/tests/BulletSeekTest.java
 */
public class EnemyCharacter implements IGameCharacter {

    private SteeringBulletEntity character;
    private SteeringBulletEntity target;


    public EnemyCharacter(Entity enemy, Entity player ){

        /*
          tmp: not going to set the controller comp on the entity for now ... enemy character will call controller methods directly
         */
        //e.add(new ControllerComponent(this.ctrl));

        CharacterComponent comp = new CharacterComponent(this, null /* tmp? */);
        enemy.add(comp);


        ICharacterControlAuto ctrl =
                new TankController(enemy.getComponent(BulletComponent.class).body,
                        enemy.getComponent(BulletComponent.class).mass /* should be a property of the tank? */);


        character = new SteeringBulletEntity(enemy, ctrl);
        character.setMaxLinearSpeed(2); // idfk
        character.setMaxLinearAcceleration(1 /* 200 */); // GN: idfk

        target = new SteeringBulletEntity(player, ctrl);


        final Seek<Vector3> seekSB = new Seek<Vector3>(character, target);
        character.setSteeringBehavior(seekSB);
    }


    @Override
    public void update(Entity entity, float deltaTime, Object whatever /* comp.lookRay */) {

        this.character.update(deltaTime);
    }
}
