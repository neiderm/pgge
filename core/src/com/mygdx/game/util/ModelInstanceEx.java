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
package com.mygdx.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/*
 * just statics now, not neant to be instanced
 */
public class ModelInstanceEx {

    // default id name is "node" + .size .. nothing fancy  see "ModelBuilder::node()"
    private static final String DEFAULT_MODEL_NODE_ID = "node1";


    /*
     * convenience wrapper to keep the temp vector axis somewhere
     */
    private static final Vector3 axis = new Vector3(); // make these temp objects final

    public static Vector3 rotateRad(Vector3 v, Quaternion rotation){

        return v.rotateRad(axis, rotation.getAxisAngleRad(axis));
    }

    public static void setColorAttribute(ModelInstance inst, Color color) {

        setColorAttribute(inst, color, color.a);
    }

    public static void setColorAttribute(ModelInstance inst, Color color, float alpha) {

        /*        ColorAttribute ca = (ColorAttribute) mat.get(ColorAttribute.Diffuse);         */

        Material mat = inst.materials.get(0);

//        if (null == mat)
//            return; // throw new GdxRuntimeException("not found");

        mat.set(ColorAttribute.createDiffuse(color));

        BlendingAttribute blendingAttribute =
                new BlendingAttribute(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, alpha);

        mat.set(blendingAttribute);
    }


    /*
     * IN:
     *
     * RETURN:
     *   ModelInstance ... which would be passed in to ModelComponent()
     *
     * Reference:
     *    https://xoppa.github.io/blog/loading-a-scene-with-libgdx/
     *    https://stackoverflow.com/questions/21827302/scaling-a-modelinstance-in-libgdx-3d-and-bullet-engine
     */
    public static ModelInstance getModelInstance(Model model, String node, Vector3 scale) {

        ModelInstance instance = new ModelInstance(model, node);

        if (null != instance)
        {
            Node modelNode = instance.getNode(node);

            if (null == modelNode){
                Gdx.app.log("ModelInstanceEx", "Failed, inst.nodes.size = " + instance.nodes.size);
                instance = null;

            } else {
                instance.transform.set(modelNode.globalTransform);
                modelNode.translation.set(0, 0, 0);
                modelNode.scale.set(1, 1, 1);
                modelNode.rotation.idt();
                instance.calculateTransforms();
            }

            if (null != scale) {
                instance.nodes.get(0).scale.set(scale);
                instance.calculateTransforms();
            }
        }
        return instance;
    }

    /*
     * convert-a-model
     *
     * creates a model that is not disposed .... !!!!!
     */
    public static Model modelFromNodes(Model model){

        // "demodularize" model - combine modelParts into single Node for generating the physics shape
        // (with a "little" work - multiple renderable instances per model component -  the model could remain "modular" allowing e.g. spinny bits on rigs)
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        MeshBuilder meshBuilder = new MeshBuilder();
        meshBuilder.begin(model.meshParts.get(0).mesh.getVertexAttributes(), GL20.GL_TRIANGLES );

        for (Node node : model.nodes) {

            if (node.parts.size > 0) {

                MeshPart meshPart = node.parts.get(0).meshPart;
                meshBuilder.setVertexTransform(node.localTransform); // apply node transformation
                meshBuilder.addMesh(meshPart);
            }
            else
                Gdx.app.log("SceneLoader ", "node.parts.size < 1 ..." + node.parts.size );
        }

        Mesh mesh = meshBuilder.end();

        // all nodes we're combining would have the same material ... I guesss
        modelBuilder.part(
                DEFAULT_MODEL_NODE_ID, mesh, GL20.GL_TRIANGLES, model.nodes.get(0).parts.get(0).material);

        return modelBuilder.end(); // TODO // model reference for unloading!!!
    }
}
