package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.mygdx.game.components.StatusComponent;

public class ExitSensor extends OmniSensor {

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        if (isTriggered){

            StatusComponent sc = target.getComponent(StatusComponent.class);

            if (null != sc){

                sc.UI.canExit = true;
            }
        }
    }
}
