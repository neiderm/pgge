package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.collision.Ray;
import com.mygdx.game.controllers.SteeringEntity;
import com.mygdx.game.util.GameEvent;

/**
 * Created by mango on 2/10/18.
 */

/*
 the custom "character controller" class (i.e. for non-dynamic collision objects) would be able
 to instantiate with inserted instance of a suitable simple controller e.g. PID controller etc.
 */
public class CharacterComponent implements Component {

    public SteeringEntity steerable;
    public GameEvent gameEvent;
    public Ray lookRay = new Ray();

    public CharacterComponent(SteeringEntity steeringEntity) {

        this.steerable = steeringEntity;
    }

    /*
     every entity instance must have its own gameEvent instance
     */
    public CharacterComponent(SteeringEntity steeringEntity, GameEvent gameEvent) {

        this(steeringEntity);
        this.gameEvent = gameEvent;
    }
}
