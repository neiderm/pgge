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
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.sceneLoader.GameFeature;
import com.mygdx.game.sceneLoader.SceneLoader;
import com.mygdx.game.util.ModelInstanceEx;

/*
 * crap you have to pick up
 */
public class Crapium extends OmniSensor {

    private Attribute saveAttribute;


    @Override
    public void init(Object obj) {

        super.init(obj);

        // ha nice hackage
        SceneLoader.numberOfCrapiums += 1;
    }


    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        if (isActivated) {

            ModelComponent mc = sensor.getComponent(ModelComponent.class);

            Material mat = null;
            if (mc.modelInst.materials.size > 0) {
                mat = mc.modelInst.materials.get(0);

//        if (null == mat)
//            return; // throw new GdxRuntimeException("not found");
                //else
//    Gdx.app.log("sdf", "sdf"); //  doesn't necessarily have a material
            }

            if (isTriggered) {

                if (null == saveAttribute) {

                    if (null != mat) {
                        // grab the color attribute
                        saveAttribute = mat.get(ColorAttribute.Diffuse);
                    }
                }

                ModelInstanceEx.setColorAttribute(mc.modelInst, new Color(Color.PURPLE)); // tmp test code

                isActivated = false;

                GameFeature playerFeature = GameWorld.getInstance().getFeature("Player");
                if (null != playerFeature) {

                    StatusComponent sc = playerFeature.getEntity().getComponent(StatusComponent.class);

                    if (null != sc) {
// TODO: here's a good one, player is being has-been destroyed while triggering this Crapium so reference might be invalid   BUG thats not easy to reproduce
                        if (null != sc.UI) {

                            sc.UI.incHitCount(1);
                        }
                        else
                            System.out.println();
                    }
                }
            } else {

                if (null != mat && null != saveAttribute) {

                    mat.set(saveAttribute);
                }
            }
        }
    }
}
