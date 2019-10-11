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
    private float sc = 1;
    private Vector3 scale = new Vector3(1, 1, 1);
    private Color cc = new Color().set(Color.RED);
//    private Texture cubeTex =  new Texture(Gdx.files.internal("data/badlogic.png"), false);
//    private Material mat = new Material(TextureAttribute.createDiffuse(cubeTex));

    @Override
    public void update(Entity burningThing) {

//        super.update(sensor);

//        if (isActivated)
        {
            ModelComponent mc = burningThing.getComponent(ModelComponent.class);

            if (clock > 0) {

                clock -= 1;
                alpha -= 0.01f;
                scale.scl(1.05f);

                mc.modelInst.nodes.get(0).scale.set(scale);
                mc.modelInst.calculateTransforms();

                if (mc.modelInst.materials.size > 0) {

                    if (false) { // could change the texture on the fly! (be sure to dispose any new Texture see below)
                        Material mmat = mc.modelInst.materials.get(0);
                        mmat.clear(); // discard existing color/texture attribs.
                        //mmat.set(TextureAttribute.createDiffuse(cubeTex));
                    } else {
                        cc.a = alpha;
//                        ModelInstanceEx.setColorAttribute(mc.modelInst, cc, alpha); // this one sets the blending attribute .. doesn't matter
                        ModelInstanceEx.setColorAttribute(mc.modelInst, cc);

//                    mmat.set(ColorAttribute.createDiffuse(cc));
//                    BlendingAttribute blendingAttribute =
//                            new BlendingAttribute(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, alpha);
//                    mmat.set(blendingAttribute);
                    }
                }
            } else {
                // kill me
//                StatusComponent sc = sensor.getComponent((StatusComponent.class));
//                if (null == sc)
                burningThing.add(new StatusComponent(true));

//                cubeTex.dispose();
            }
        }
    }
}
