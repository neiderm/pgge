package com.mygdx.game.features;

import com.mygdx.game.components.StatusComponent;

public class ExitSensor extends OmniSensor {

    @Override
    public boolean getIsTriggered(){

        if (isTriggered){
            StatusComponent sc = target.getComponent(StatusComponent.class);

            if (null != sc){

                sc.UI.canExit = true;
            }
        }
        return isTriggered;
    }
}
