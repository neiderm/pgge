package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.util.ModelInstanceEx;

public class ExitSensor extends OmniSensor {

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        if (isActivated) {

            ModelComponent mc = sensor.getComponent(ModelComponent.class);

            if (mc.modelInst.materials.size > 0)
                ModelInstanceEx.setColorAttribute(mc.modelInst, new Color(Color.TEAL)); // tmp test code
//else
//    Gdx.app.log("sdf", "sdf"); //  doesn't necessarily have a material

            if (isTriggered) {

                StatusComponent sc = target.getComponent(StatusComponent.class);

                if (null != sc.UI) {

                    sc.UI.canExit = true;
                } else
                    Gdx.app.log("barf", "gacked");
            }
        }
    }
}
