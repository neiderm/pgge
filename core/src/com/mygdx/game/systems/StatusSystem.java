package com.mygdx.game.systems;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.DeleteMeComponent;
import com.mygdx.game.components.StatusComponent;

/**
 * Created by neiderm on 7/5/2018.
 */

public class StatusSystem extends IteratingSystem {

    public StatusSystem() {

        super(Family.all(StatusComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        if (!GameWorld.getInstance().getIsPaused()) {  // would like to allow engine to be actdive if ! paused but on-screen menu is up

            StatusComponent comp = entity.getComponent(StatusComponent.class);

//        if (null != comp) 
            {
                if (null != comp.featureIntrf) {
                    comp.featureIntrf.update(entity);
                }
            }

            if (comp.lifeClock > 0) {

                comp.lifeClock -= 1;
            }
            else //       if (comp.lifeClock == 0)
            {
                if (comp.dieClock > 0) {

                    comp.dieClock -= 1;

                    if (1 == comp.dieClock) {
                        // really die
                        if (comp.isEntityRemoveable) { // can be removed ...

                            Component deleteMe = entity.getComponent(DeleteMeComponent.class);

                            if (null != deleteMe) {
                                entity.getComponent(DeleteMeComponent.class).deleteMe = true;
                            }
                        }
                    }
                }
            }
        }
    }

//    private void messWithColor(ModelInstance instance) {
//
//        Material material = instance.materials.get(0);
//        ColorAttribute ca = (ColorAttribute) material.get(ColorAttribute.Diffuse);
//
//        float dR = 0.01f;
//        float dG = 0.00f;
//        float dB = 0.01f;
//
//        if (ca.color.a < 0.9f) {
//            ca.color.a += 0.005f;
//        }
//        if (ca.color.r < 1.0) {
//            ca.color.r += dR;
//        }
//        if (ca.color.g < 1.0) {
//            ca.color.g += dG;
//        }
//        if (ca.color.b < 1.0) {
//            ca.color.b += dB;
//        }
//
//        ModelInstanceEx.setColorAttribute(instance, ca.color);
//    }
}
