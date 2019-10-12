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
    private Color cc = new Color().set(Color.RED);
//    private Texture cubeTex =  new Texture(Gdx.files.internal("data/badlogic.png"), false);
//    private Material mat = new Material(TextureAttribute.createDiffuse(cubeTex));

    private StatusComponent sc = null;

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

                mc.modelInst.nodes.get(0).scale.set(scale);
                mc.modelInst.calculateTransforms();

                if (mc.modelInst.materials.size > 0) {

//                    Material mmat = mc.modelInst.materials.get(0);

                    if (false) { // could change the texture on the fly! (be sure to dispose any new Texture see below)
//                        mmat.clear(); // discard existing color/texture attribs.
                        //mmat.set(TextureAttribute.createDiffuse(cubeTex));
                    } else {
                        cc.a = alpha;
                        ModelInstanceEx.setColorAttribute(mc.modelInst, cc); // this one sets the blending attribute .. doesn't matter
                    }
                }
            } else {     // kill me
//                StatusComponent sc = sensor.getComponent((StatusComponent.class));
//                if (null == sc)
///*
                ee.add(new StatusComponent(true));
//*/
//                cubeTex.dispose();
            }
        }
    }
}
