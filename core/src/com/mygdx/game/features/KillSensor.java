package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.mygdx.game.components.StatusComponent;

/**
 * Created by neiderm on 7/5/2019.
 * <p>
 * This can be a "generic" handler for a sensor. assigned a single target Entity to be sensing for.
 */

public class KillSensor extends OmniSensor {

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        if (isTriggered){
            target.getComponent(StatusComponent.class).lifeClock = 0;
        }
    }
}
