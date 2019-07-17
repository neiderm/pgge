package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.controllers.SteeringEntity;

/**
 * Created by neiderm on 2/10/18.
 */

/*
 the custom "character controller" class (i.e. for non-dynamic collision objects) would be able
 to instantiate with inserted instance of a suitable simple controller e.g. PID controller etc.
 */
public class CharacterComponent implements Component {

    public SteeringEntity steerable;
    public boolean isPlayer;

    public CharacterComponent(){ /* mt */
    }

    public CharacterComponent(SteeringEntity steeringEntity) {

        setSteerable(steeringEntity);
    }

    public void setSteerable (SteeringEntity steeringEntity) {

        this.steerable = steeringEntity;
    }
}
