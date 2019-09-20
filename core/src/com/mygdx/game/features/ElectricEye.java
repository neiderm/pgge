package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.util.ModelInstanceEx;

/*
 * Maybe for starters just draw a red line and player becomes dead
 */
public class ElectricEye extends VectorSensor {


    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        if (isActivated) {

            ModelComponent mc = sensor.getComponent(ModelComponent.class);
            if (mc.modelInst.materials.size > 0) ModelInstanceEx.setColorAttribute(mc.modelInst, new Color(Color.ROYAL)); // tmp test code
//else
//    Gdx.app.log("sdf", "sdf"); //  doesn't necessarily have a material

            if (isTriggered) {
//                Vector3 trans = new Vector3();
//                mc.modelInst.transform.getTranslation(trans);
                if (mc.modelInst.materials.size > 0) {
                    ModelInstanceEx.setColorAttribute(mc.modelInst, new Color(Color.GOLD)); // tmp test code
                }



                isTriggered = false; // idfk ... unlatch it ourselfves i guess
            }
        }
    }
}
