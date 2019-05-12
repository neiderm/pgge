package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.util.ModelInstanceEx;

/**
 * Created by utf1247 on 7/5/2018.
 */

public class StatusSystem extends IteratingSystem {

    public StatusSystem() {
        super(Family.all(StatusComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        StatusComponent comp = entity.getComponent(StatusComponent.class);

//        if (null != comp) 
        {
            if (null != comp.statusUpdater) {
                comp.statusUpdater.update(entity);
            }
        }


        if (comp.lifeClock > 0) {

            comp.lifeClock -= 1;
//            comp.dieClock = StatusComponent.DIECLOCKDEFAULT;
        } else //       if (comp.lifeClock == 0)
        {
            if (comp.dieClock > 0) {
                comp.dieClock -= 1;
            }

            // TODO: model utiltiies

            ModelInstance inst = entity.getComponent(ModelComponent.class).modelInst;

            Material material = inst.materials.get(0);

            ColorAttribute ca = (ColorAttribute) material.get(ColorAttribute.Diffuse);

            if (ca.color.a >= 0.01f)
                ca.color.a -= 0.005f;

            if (0 == comp.dieClock) {
                // really die
                ca.color.a = 0;

//Gdx.app.log("were doomed","");

//                engine.putInRemovalQueue(entity);

                BulletComponent bc = entity.getComponent(BulletComponent.class);
                if (null != bc){
bc.body.translate(new Vector3(5, 5, 5));
                }

                inst.transform.setTranslation(100, 100, 100);
            }

            ModelInstanceEx.setColorAttribute(inst, ca.color);
        }

    }
}
