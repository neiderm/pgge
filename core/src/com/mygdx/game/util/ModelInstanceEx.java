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
package com.mygdx.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/*
 * just statics now, not intended to be instanced
 */
public class ModelInstanceEx {

    private ModelInstanceEx() {
    }

    /*
     * convenience wrapper to keep the temp vector axis somewhere
     */
    private static final Vector3 axis = new Vector3();

    public static Vector3 rotateRad(Vector3 v, Quaternion rotation) {

        return v.rotateRad(axis, rotation.getAxisAngleRad(axis));
    }

    public static void setColorAttribute(ModelInstance inst, Color color) {

        setColorAttribute(inst, color, color.a);
    }

    public static void setColorAttribute(ModelInstance inst, Color color, float alpha) {

        Material mat = inst.materials.get(0);

//        if (null == mat)
//            return; // throw new GdxRuntimeException("not found");
        mat.set(ColorAttribute.createDiffuse(color));

        BlendingAttribute blendingAttribute =
                new BlendingAttribute(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, alpha);

        mat.set(blendingAttribute);
    }

    /**
     * Reference:
     * https://xoppa.github.io/blog/loading-a-scene-with-libgdx/
     * https://stackoverflow.com/questions/21827302/scaling-a-modelinstance-in-libgdx-3d-and-bullet-engine
     *
     * @param model       model
     * @param strNodeName node name
     * @param scale       scale factor
     * @return Model Instance
     */
    public static ModelInstance getModelInstance(Model model, String strNodeName, Vector3 scale) {

        if (null == strNodeName) {
            return null; // invalid node name are handled ok, but not if null, so gth out~!
        }
        ModelInstance instance = new ModelInstance(model, strNodeName);

        Node modelNode = instance.getNode(strNodeName);

        // evidently the Node Name is not valid!
        if (null == modelNode) {
            Gdx.app.log(
                    "ModelInstanceEx: ",
                    "Can't build \"" + strNodeName + "\" null == modelNode, inst.nodes.size = " + instance.nodes.size);
            instance = null;

        } else {
            instance.transform.set(modelNode.globalTransform);
            modelNode.translation.set(0, 0, 0);
            modelNode.scale.set(1, 1, 1);
            modelNode.rotation.idt();
            instance.calculateTransforms();
        }

        // definately don't do this if instance null!
        if (null != instance && null != scale) {

            instance.nodes.get(0).scale.set(scale);

            //maybe it doesn't matter if calculator transforms is done irregardless
            instance.calculateTransforms();
        }
        return instance;
    }
}
