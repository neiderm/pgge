package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.util.ModelInstanceEx;

public class ExitSensor extends OmniSensor {

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        if (isTriggered){

            ModelComponent mc = sensor.getComponent(ModelComponent.class);
            ModelInstance mi = mc.modelInst;
            ModelInstanceEx.setColorAttribute(mi, new Color(Color.FIREBRICK)); // tmp

            StatusComponent sc = target.getComponent(StatusComponent.class);

            if (null != sc.UI){

                sc.UI.canExit = true;
            }
            else
                Gdx.app.log("barf", "gacked");
        }
    }
}
