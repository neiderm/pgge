package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.DeleteMeComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.util.ModelInstanceEx;

/**
 * Created by utf1247 on 7/5/2018.
 */

public class StatusSystem extends IteratingSystem {

    private Vector3 translation = new Vector3();


    public StatusSystem() {
        super(Family.all(StatusComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        if (!GameWorld.getInstance().getIsPaused()) {  // would like to allow engine to be actdive if ! paused but on-screen menu is up

            StatusComponent comp = entity.getComponent(StatusComponent.class);

//        if (null != comp) 
            {
                if (null != comp.statusUpdater) {
                    comp.statusUpdater.update(entity);
                }
            }

            if (comp.lifeClock > 0) {

                if (1 == comp.lifeClock)
                    Gdx.app.log("Status", "comp.lifeClock == " + comp.lifeClock);

                comp.lifeClock -= 1;
            }
            else //       if (comp.lifeClock == 0)
            {
                if (comp.dieClock > 0) {
                    comp.dieClock -= 1;
                }

                BulletComponent bc = entity.getComponent(BulletComponent.class);
                ModelInstance instance = entity.getComponent(ModelComponent.class).modelInst;
                //                instance.transform.setTranslation(0, 8, -7);
                translation.set(0, 0, 0.075f);
                instance.transform.trn(translation); // only moves visual model, not the body!

                messWithColor(instance);

                if (null != bc) {
                    bc.body.setWorldTransform(instance.transform);
                }

                if (1 == comp.dieClock) {
                    // really die
                    Gdx.app.log("Status", "comp.dieClock == " + comp.dieClock);
                    entity.add(new DeleteMeComponent());
                }
            }
        }
    }

    private void messWithColor(ModelInstance instance) {

        Material material = instance.materials.get(0);
        ColorAttribute ca = (ColorAttribute) material.get(ColorAttribute.Diffuse);

        float dR = 0.01f;
        float dG = 0.00f;
        float dB = 0.01f;

        if (ca.color.a < 0.9f) {
            ca.color.a += 0.005f;
        }
        if (ca.color.r < 1.0) {
            ca.color.r += dR;
        }
        if (ca.color.g < 1.0) {
            ca.color.g += dG;
        }
        if (ca.color.b < 1.0) {
            ca.color.b += dB;
        }

        ModelInstanceEx.setColorAttribute(instance, ca.color);
    }
}
