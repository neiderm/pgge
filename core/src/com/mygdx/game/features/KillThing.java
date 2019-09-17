package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.util.ModelInstanceEx;

/*
 * ....Feature  "DroppedBody" ..  .......... becomes un-dynamic
 * Presently, this one  strips off the physics body, it does NOT respawn as a new entity.
 */
public class KillThing extends KillSensor {

    @Override
    public void onProcessedCollision(Entity ee){

        // toooooooooodooo ... can it have option when to activate or detonate (bomb vs. mine?)

//        super.onProcessedCollision(ee);
        CompCommon.physicsBodyMarkForRemoval(ee);

        ModelComponent mc = ee.getComponent(ModelComponent.class);
        ModelInstance mi = mc.modelInst;
        if (mi.materials.size > 0) {
            ModelInstanceEx.setColorAttribute(mi, new Color(Color.RED), 0.9f); //tmp?
        }
//        else
//            Gdx.app.log("asdf", "asdf");
    }
}
