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
    private float alpha = 0.99f;
    private Vector3 scale = new Vector3(1, 1, 1);

    private Color cc = new Color(Color.ORANGE);

    // save original model material attributes ? big hack!@
    private TextureAttribute fxTextureAttrib;
    private Texture myTexture;


    public BurnOut() { // mt
    }

    private BurnOut(Material mat) {

        if (null != mat && mat.size() > 0) {

//            TextureAttribute tmpTa = (TextureAttribute) mat.get(TextureAttribute.Diffuse); // idfk
//
//            if (null != tmpTa)
//            {
//// here ... if tmpTa == null then create a new default Texture ... or Color
//                userData = tmpTa; // big-ass hack
///*
//                Texture texture = tmpTa.textureDescription.texture;
//                ta = TextureAttribute.createDiffuse(texture);
//*/
//            } else
                {
//                userData = 0xdeadbeef;
                userData = mat;
            }
        }
    }

    public BurnOut(ModelInstance mi) {

        this(mi.materials.get(0));
    }

    @Override
    public void init(Object object) {

        if (null == userData) {

            userData = object;
        } else {
            System.out.println("never!");
        }

        if (null != object) {

//            Class c = object.getClass();
//
///* this is transitional thing for playing around ... its just a matter of settling on Material or whatever
//as User Data type
// */
//            if (c.toString().contains("attributes.TextureAttribute"))  // lazy , discard the full class path
//            {
//                fxTextureAttrib = (TextureAttribute) object;
//            }
//
//            if (c.toString().contains("g3d.Material"))  // lazy , discard the full class path
            {
                Material saveMat = (Material) object;

                TextureAttribute tmpTa = (TextureAttribute) saveMat.get(TextureAttribute.Diffuse);

                if (null != tmpTa) {
                    myTexture = tmpTa.textureDescription.texture;

                    fxTextureAttrib = TextureAttribute.createDiffuse(myTexture);
                }
                else {
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
            // Status System provides a die timer ... that would be fine, as once we are done we are finally done (so it's a simple dleteion)
/*
            if (null == sc){
                sc = new StatusComponent(null, 0, 3);
                ee.add( sc );
            }
*/
            ModelComponent mc = ee.getComponent(ModelComponent.class);

            if (clock > 0) {

                clock -= 1;     // could have a Status Comp provide this timer eh????

                alpha -= 0.01f;
                scale.scl(1.010f);

                ModelInstance mi = mc.modelInst;

                mi.nodes.get(0).scale.set(scale);
                mi.calculateTransforms();

                if (mi.materials.size > 0) {

                    Material mat = mi.materials.get(0);

                    // make sure Material is valid  i.e. should have at least 1 Attribute
                    if (null != mat && mat.size() > 0
                            && null != fxTextureAttrib) {

                        mat.set(fxTextureAttrib); // idfk i guess don't care what tex coords are just smearing it around the shape anyway

//                        BlendingAttribute blendingAttribute = new BlendingAttribute(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, alpha);
//                        mat.set(blendingAttribute);
                    } else
                    {
                        // just mess w/ color
                        mat.clear();
                    }
                    // and for the icing on this bitcake ... fade it out!!! whoooh ooo
                    cc.a = alpha;
                    ModelInstanceEx.setColorAttribute(mi, cc); // this one sets the blending attribute .. doesn't matter
                }
            } else {     // kill me
//                StatusComponent sc = sensor.getComponent((StatusComponent.class));
//                if (null == sc)
///*
                ee.add(new StatusComponent(true));
//*/
                // check if we we're  using a "local" Texture  ( ??? wtfe )     and if so dispose()
                if (null != myTexture) {
                    myTexture.dispose(); // idfk
                }
            }
        }
    }
}
