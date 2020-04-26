/*
 * Copyright (c) 2019 Glenn Neidermeier
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
 * quick and dirty, very simple "exploding" effect
 */
public class BurnOut extends FeatureAdaptor {

    private Vector3 up = new Vector3( );

    private ModelComponent mc = null;

    private int clock = 88;  // using as percent alphA
    private Vector3 scale = new Vector3(1, 1, 1);
    private Color cc;

    // save original model material attributes ? big hack!@
    private TextureAttribute fxTextureAttrib;
//    private Texture myTexture; // may not use this

    public BurnOut() { // mt
    }

    public BurnOut(TextureAttribute fxTextureAttrib, KillSensor.ImpactType impt) {

        this.fxTextureAttrib = fxTextureAttrib;
        this.activateOnState = GameWorld.GAME_STATE_T.ROUND_ACTIVATE_ON_ALL;

        if (KillSensor.ImpactType.ACQUIRE == impt) {
            // marker for prize pickup
            cc = new Color(Color.GOLD);
        }
        else  if (KillSensor.ImpactType.POWERUP == impt)
        {
            cc = new Color(Color.PURPLE);
        }
        else  if (KillSensor.ImpactType.DAMAGING == impt)
        {
            cc = new Color(Color.YELLOW);
        }
        else  if (KillSensor.ImpactType.STRIKE == impt)
        {
            cc = new Color(Color.ROYAL);
        }
        else { // FATAL etc.
            cc = new Color(Color.FIREBRICK);
        }

        if (KillSensor.ImpactType.DAMAGING == impt || KillSensor.ImpactType.FATAL == impt) {

// these will sloooowwly float up
            up.set(0, 0.01f, 0);
        }
    }


    @Override
    public void update(Entity ee) {

        super.update(ee);

        if (null == mc){
            mc = ee.getComponent(ModelComponent.class);
            ModelInstance mi = mc.modelInst;
            scale.set( mi.nodes.get(0).scale);
        }

        //        if (isActivated)
        {
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

                    Material mat = mi.materials.get(0);

                    // make sure Material is valid  i.e. should have at least 1 Attribute
                    if (null != mat && mat.size() > 0 && null != fxTextureAttrib) {

                        mat.set(fxTextureAttrib); // idfk i guess don't care what tex coords are just smearing it around the shape anyway
                    } else {
                        // just mess w/ color
                        mat.clear();
                    }
                    // and for the icing on this bitcake ... fade it out!!! whoooh ooo
                    cc.a = clock / 100.0f;

                    ModelInstanceEx.setColorAttribute(mi, cc); // this one sets the blending attribute .. doesn't matter
                }
            } else {
                // kill me  - most of the time not having to protect against re-adding the Status Comp, but this one would't stay dead :(
                StatusComponent sc = ee.getComponent((StatusComponent.class));
                if (null == sc) {
                    ee.add(new StatusComponent(0));
                }
                else{
                    ee.getComponent(StatusComponent.class).lifeClock = 0;
                }
            }
        }
    }
}
