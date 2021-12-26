/*
 * Copyright (c) 2021 Glenn Neidermeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.util.ModelInstanceEx;

/*
 * use 3d sphere and colors to simulate simple explosion effect for various entities
 */
public class BurnOut extends FeatureAdaptor {

    private final Color cc;
    private final Vector3 scale = new Vector3(1, 1, 1);
    private final Vector3 up = new Vector3();
    // save original model material attributes ? big hack!
    private final TextureAttribute fxTextureAttrib;

    private ModelComponent mc = null;
    private int clock = 88;  // using as percent alphA
    private String key;

    BurnOut(TextureAttribute fxTextureAttrib, KillSensor.ImpactType impt) {

        this.fxTextureAttrib = fxTextureAttrib;
        this.activateOnState = GameWorld.GAME_STATE_T.ROUND_ACTIVATE_ON_ALL;

        switch (impt) {
            default:
            case ACQUIRE:
                // marker for prize pickup
                key = "000";
                cc = new Color(Color.GOLD);
                break;
            case POWERUP:
                key = "001";
                cc = new Color(Color.PURPLE);
                break;
            case DAMAGING:
                key = "002";
                cc = new Color(Color.YELLOW);
                up.set(0, 0.01f, 0); // slowly float up
                break;
            case STRIKE:
                key = "003";
                cc = new Color(Color.ROYAL);
                break;
            case FATAL:
                key = "004";
                cc = new Color(Color.FIREBRICK);
                up.set(0, 0.01f, 0); // slowly float up
                break;
        }
    }

    @Override
    public void onDestroyed(Entity e) { // MT
    }

    @Override
    public void update(Entity ee) {

        super.update(ee);

        if (null == mc) {
            mc = ee.getComponent(ModelComponent.class);
            ModelInstance mi = mc.modelInst;
            scale.set(mi.nodes.get(0).scale);

            Vector3 slocation = new Vector3();
            GameWorld.AudioManager.playSound(key, mi.transform.getTranslation(slocation));
        }
        //        if (isActivated)
        if (clock > 0) {

            clock -= 1;     // could have a Status Comp provide this timer eh????
            final float d_ALPHA = 0.01f;
            scale.scl(/*1.010f*/ 1.0f + d_ALPHA);

            // if (null != mc) {
            ModelInstance mi = mc.modelInst;
            mi.nodes.get(0).scale.set(scale);
            // make'er float away
            mi.transform.trn(up);
            mi.calculateTransforms();

            if (mi.materials.size > 0) {
                // could cache material?
                Material mat = mi.materials.get(0);

                // make sure material is valid i.e. should have at least 1 attribute
                if (null != mat && mat.size() > 0 && null != fxTextureAttrib) {
                    mat.set(fxTextureAttrib); // idfk i guess don't care what tex coords are just smearing it around the shape anyway
                } else {
                    // just mess w/ color
                    assert mat != null;
                    mat.clear();
                }
                // and for the icing on this bitcake ... fade it out!!! whoooh ooo
                cc.a = clock / 100.0f;
                ModelInstanceEx.setColorAttribute(mi, cc); // this one sets the blending attribute .. doesn't matter
            }
        } else {
            // kill me - most of the time not having to protect against re-adding the Status Comp, but this one would't stay dead :(
            StatusComponent sc = ee.getComponent((StatusComponent.class));
            if (null == sc) {
                ee.add(new StatusComponent(0));
            } else {
                ee.getComponent(StatusComponent.class).lifeClock = 0;
            }
        }
    }
}
