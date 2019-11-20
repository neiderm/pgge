package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.mygdx.game.components.StatusComponent;

/**
 * Created by neiderm on 8/24/2019.
 * <p>
 * Out-of-bounds sensor is simply an inverted kill sensor ...
 *  or was ..... tmp ?  possibly re-integrate as sub-class of KS.
 */
public class OOBSensor /*extends KillSensor*/ extends OmniSensor {

    @Override
    public void init(Object obj){

        super.init(obj);

        inverted = true;
    }

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        if (isTriggered) {
            target.getComponent(StatusComponent.class).lifeClock = 0;
        }
    }
}
