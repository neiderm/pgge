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
package com.mygdx.game.sceneLoader;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GameWorld;
import com.mygdx.game.util.PrimitivesBuilder;

/**
 * Created by neiderm on 12/18/17.
 */

public class ModelGroup {

    public static final String SPAWNERS_MGRP_KEY = "Spawners";
    public static final String MGRP_DEFAULT_MDL_NAME = ""; // if MG key is "empty", then multiple gsme Objectca

    private static final String DEFAULT_MODEL_NODE_ID = "node1";


    public ModelGroup() {
    }

    public ModelGroup(String modelName) {
        this.modelName = modelName; // try setting this now
    }

    Array<GameObject> elements = new Array<GameObject>();
    private String modelName;
    private boolean isKinematic;
    private boolean isCharacter;


    /*
     * iterate all GameObjects in this instance and build them
     *
     */
    public void build(Engine engine, boolean deleteObjects) {

        build(engine);

        if (deleteObjects) {
            elements.clear();
        }
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void addElement(GameObject object) {
        elements.add(object);
    }

    public GameObject getElement(int index) {

        if (elements.size > 0) {

            return elements.get(index);

        } else
            return null;
    }

    public GameObject getElement(String name) {

        GameObject found = null;

        for (GameObject gameObject : elements) {
            if (gameObject.objectName.equals(name))
                found = gameObject;
        }
        return found;
    }

    void build(Engine engine) {

        SceneData sd = GameWorld.getInstance().getSceneData();
        ModelInfo mi = sd.modelInfo.get(this.modelName);
        Model groupModel = null;

        if (null != mi) {

            groupModel = mi.model; // should maybe check model valid ;)

            if (null == mi.model && null != mi.fileName) {

                Gdx.app.log("ModelGroup", "Not a valid model! (null == mi.model && null != mi.fileName)");
                return; // for now ... ?
            }
        }

        for (GameObject gameObject : elements) {

            if (null == this.modelName && null == gameObject.objectName) { // one of these has to be true in order to get model info !
                System.out.println("null == this.modelName && null == gameObject.objectName");
                continue; // break on localPlayer modelGroup as it is a "dummy"
            }

            if (this.isKinematic) {
                gameObject.isKinematic = this.isKinematic;
            }
            if (this.isCharacter) {
                gameObject.isCharacter = this.isCharacter;
            }

            Model model;

            if (null == groupModel) {

                String rootNodeId;
                btCollisionShape shape = null;
                ModelInstance instance;

                // look for model Info name matching object name ... seems only reason is so a model
                // group of objects can be grouped together and set common attribute ... ischaractrer
                ModelInfo mdlInfo = sd.modelInfo.get(gameObject.objectName);

                if (null != mdlInfo) {

                    model = mdlInfo.model;

                    if (model.nodes.size > 1) { // multi-node model

                        instance = new ModelInstance(model);
                        // creates the cvx hull shape by combining into single mesh from multi-node model
//  TRY_COMP
//                      shape = PrimitivesBuilder.getShape(model);

                    } else {
                        rootNodeId = model.nodes.get(0).id;
                        instance = new ModelInstance(model, rootNodeId);
//  TRY_COMP
  //                    shape = PrimitivesBuilder.getShape(model.meshes.get(0)); // createConvexHullShape and saves the mesh Shape ref
                    }

                    if ( true/*TRY_COMP*/) {
//                        if (0 != gameObject.mass  )
                        { // gets a compound bullet shape
                            shape = PrimitivesBuilder.getShape(model, true);
                        }
                    }
                } else {
                    model = PrimitivesBuilder.getModel();
                    rootNodeId = gameObject.objectName;

                    Vector3 v3scale = gameObject.scale;

                    if (null != rootNodeId) {
// doesn't protect itself again null node name
                        instance = new ModelInstance(model, rootNodeId);
                    } else {
                        instance = new ModelInstance(model); // probably no good!!!!!!!
                    }

                    // if model instance invalid (0 nodes) then make it a non graphical entity
                    if ((instance.nodes.size > 0)) {

                        if (null != v3scale && null != instance) {
                            instance.nodes.get(0).scale.set(v3scale);
                            instance.calculateTransforms();
                        }

                        // note does not use the gamObject.meshSHape name
                        shape = PrimitivesBuilder.getShape(rootNodeId, v3scale);

                    } else {
                        instance = null; // don't use the invalid instance and a non graphical or physics  entity is created
                    }
                }

                gameObject.buildGameObject( engine, instance, shape);

            } else {
                /* load all nodes from model that match /objectName.*/
                gameObject.buildNodes(engine, groupModel);
            }
        }
    }
}
