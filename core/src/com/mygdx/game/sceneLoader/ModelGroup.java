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
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;
import com.mygdx.game.util.PrimitivesBuilder;

/**
 * Created by neiderm on 12/18/17.
 */

public class ModelGroup {

    public static final String SPAWNERS_MGRP_KEY = "Spawners";
    public static final String MGRP_DEFAULT_MDL_NAME = ""; // if MG key is "empty", then multiple gsme Objectca

    private static final String DEFAULT_MODEL_NODE_ID = "node1";


    ModelGroup() {
    }

    public ModelGroup(String modelName) {
        this.modelName = modelName; // try setting this now
    }

    ModelGroup(String groupName, String modelName) {

        this(groupName);
        this.modelName = modelName;
        this.isKinematic = true;
    }

    Array<GameObject> gameObjects = new Array<GameObject>();
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
            gameObjects.clear();
        }
    }

    public String getModelName() {
        return modelName;
    }

    public void addGameObject(GameObject object) {
        gameObjects.add(object);
    }

    public GameObject getGameObject(int index) {

        if (gameObjects.size > 0) {

            return gameObjects.get(index);

        } else
            return
                    null;
    }

    public GameObject getGameObject(String name) {

        GameObject found = null;

        for (GameObject gameObject : gameObjects) {
            if (gameObject.objectName.equals(name))
                found = gameObject;
        }
        return found;
    }

    void build(Engine engine) {

        SceneData sd = GameWorld.getInstance().getSceneData();
        ModelInfo mi = sd.modelInfo.get(this.modelName); // mg can't be null ;)
        Model groupModel = null;

        if (null != mi) {

            groupModel = mi.model; // should maybe check model valid ;)


            if (null == mi.model && null != mi.fileName) {

                Gdx.app.log("ModelGroup", "Not a valid model! (null == mi.model && null != mi.fileName)");
                return; // for now ... ?
            }

        } else if (null == this.modelName && 0 == this.gameObjects.size) {

            return; // bah
        }


        for (GameObject gameObject : gameObjects) {

            if (this.isKinematic) {
                gameObject.isKinematic = this.isKinematic;
            }
            if (this.isCharacter) {
                gameObject.isCharacter = this.isCharacter;
            }
            if (null == gameObject.scale) {
                gameObject.scale = new Vector3(1, 1, 1);
            }

            Model model;

            if (null == groupModel) {

                String rootNodeId;
                btCollisionShape shape;
                ModelInstance instance;

                // look for model Info name matching object name
                ModelInfo mdlInfo = sd.modelInfo.get(gameObject.objectName);

                if (null != mdlInfo) {

                    model = mdlInfo.model;
// no null model here ;)
                    rootNodeId = model.nodes.get(0).id;

                    if (model.nodes.size > 1) { // multi-node model
// vehicle models are made to explode to each is in own g3db subdivided into meshparts/nodes. Since I don't know anybetter the GfxUtil is there as helper to track the new Model()
// since the Model is created outside of asset Loader
                        // "demodularize" model - combine modelParts into single Node for generating the physics shape
                        Model newModel = GfxUtil.modelFromNodes(model); // TODO // model reference for unloading!!!

                        rootNodeId = DEFAULT_MODEL_NODE_ID;

                        instance = ModelInstanceEx.getModelInstance(newModel /* NEW MODEL ! */, rootNodeId, gameObject.scale);
                        shape = PrimitivesBuilder.getShape(newModel.meshes.get(0)); //TODO we would only use this for generating the sHAPE (modelComps to be multi-model-instance)
                    } else {
                        instance = ModelInstanceEx.getModelInstance(model, rootNodeId, gameObject.scale);
                        shape = PrimitivesBuilder.getShape(model.meshes.get(0));
                    }
                } else {
                    model = PrimitivesBuilder.getModel();
                    rootNodeId = gameObject.objectName;
                    // note does not use the gamObject.meshSHape name
                    shape = PrimitivesBuilder.getShape(gameObject.objectName, gameObject.scale); // note: 1 shape re-used
                    instance = ModelInstanceEx.getModelInstance(model, rootNodeId, gameObject.scale);
                }
                gameObject.buildGameObject(model, engine, instance, shape);
            } else {
                /* load all nodes from model that match /objectName.*/
                gameObject.buildNodes(engine, groupModel);
            }
        }
    }
}
