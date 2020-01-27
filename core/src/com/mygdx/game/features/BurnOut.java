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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.util.ModelInstanceEx;

/*
 * quick and dirty, very simple "exploding" effect
 */
public class BurnOut extends FeatureAdaptor {

    private int clock = 128;
    private Vector3 scale = new Vector3(1, 1, 1);
    private Color cc;

    // save original model material attributes ? big hack!@
    private TextureAttribute fxTextureAttrib;
//    private Texture myTexture; // may not use this

    public BurnOut() { // mt
    }

    public BurnOut(ModelInstance mi, KillSensor.ImpactType useFlags) {

        setColorTex(mi, useFlags);
    }

    private void setColorTex(ModelInstance modelInstance, KillSensor.ImpactType useFlags) {

        if (null != modelInstance) {
//            Class c = object.getClass();
//            if (c.toString().contains("g3d.Material"))  // lazy , discard the full class path
            {
                cc = new Color(Color.FIREBRICK);

                if (KillSensor.ImpactType.ACQUIRE == useFlags) { // marker for prize pickup
                    cc = new Color(Color.SKY); // hacky hackhackster
                } else  if (KillSensor.ImpactType.DAMAGING == useFlags)
                { // marker for hit/collision w/ damage
                    cc = new Color(Color.YELLOW);
                }

                Material saveMat = modelInstance.materials.get(0);

                TextureAttribute tmpTa = (TextureAttribute) saveMat.get(TextureAttribute.Diffuse);

                if (null != tmpTa) {

                    Texture tt = tmpTa.textureDescription.texture;
                    fxTextureAttrib = TextureAttribute.createDiffuse(tt);
                } else {
/*
                    myTexture = new Texture("data/crate.png"); // tmp test
                        ta = TextureAttribute.createDiffuse(myTexture);
*/
                }
            }
        }
    }

    @Override
    public void update(Entity ee) {

//        super.update(sensor);

//        if (isActivated)
        {
            if (clock > 0) {

                clock -= 1;     // could have a Status Comp provide this timer eh????
                final float d_ALPHA = 0.01f;
                scale.scl(/*1.010f*/ 1.0f + d_ALPHA);

                ModelComponent mc = ee.getComponent(ModelComponent.class);
                // if (null != mc) {
                ModelInstance mi = mc.modelInst;
                mi.nodes.get(0).scale.set(scale);
                mi.calculateTransforms();

                if (mi.materials.size > 0) {

                    Material mat = mi.materials.get(0);

                    // make sure Material is valid  i.e. should have at least 1 Attribute
                    if (null != mat && mat.size() > 0
                            && null != fxTextureAttrib) {

                        mat.set(fxTextureAttrib); // idfk i guess don't care what tex coords are just smearing it around the shape anyway
                    } else {
                        // just mess w/ color
                        mat.clear();
                    }
                    // and for the icing on this bitcake ... fade it out!!! whoooh ooo
                    cc.a -= d_ALPHA;
                    ModelInstanceEx.setColorAttribute(mi, cc); // this one sets the blending attribute .. doesn't matter
                }
            } else {
                // kill me  - most of the time not having to protect against re-adding the Status Comp, but this one would't stay dead :(
                StatusComponent sc = ee.getComponent((StatusComponent.class));
                if (null == sc) {
                    ee.add(new StatusComponent(0));
                }

//                // check if we we're  using a "local" Texture  ( ??? wtfe )     and if so dispose()
//                if (null != myTexture) {
//                    myTexture.dispose(); // idfk
//                }
            }
        }
    }
}
