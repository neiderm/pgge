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

import com.badlogic.gdx.utils.Array;

/*
 * some kind of hacked-up Game Object container
 */

public class ModelGroup {

    ModelGroup() {
    }

    public ModelGroup(String groupName) {
    }

    ModelGroup(String groupName, String modelName) {

        this(groupName);
        this.modelName = modelName;
        this.isKinematic = true;
    }

    public String modelName;
    public Array<GameObject> gameObjects = new Array<GameObject>();

    public boolean isKinematic;
    public boolean isCharacter;


    /*
     * iterate all GameObjects in this instance and build them
     *
     */
    public void build(boolean deleteObjects){

        build();

        if (deleteObjects){

            gameObjects.clear();
        }
    }

    /*
     * For now let be simple case of spawning in game objects
     * (eventually it ideally to commonize w/ sceneLoader but that is a ways off ...
     */
    public void build(){

        for (GameObject obj : gameObjects){


            /* could end up "gameObject.build()" ?? */

//            buildObjectInstance(
//             ModelInstance instance, GameObject gameObject, btCollisionShape shape, InstanceData id) {
        }
    }
}
