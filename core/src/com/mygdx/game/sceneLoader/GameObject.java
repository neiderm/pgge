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

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class GameObject {

    GameObject() {
    }

    public GameObject(String objectName, String meshShape) {
        this.objectName = objectName;
        this.meshShape = meshShape;
        this.isShadowed = true;
//        this.isKinematic = true;
        this.isPickable = false;
        this.scale = new Vector3(1, 1, 1); // placeholder
    }

    public Array<InstanceData> instanceData = new Array<InstanceData>();
    public String objectName;
    //            Vector3 translation; // needs to be only per-instance
    public Vector3 scale; // NOT per-instance, all instances should be same scale (share same collision Shape)
    public float mass;
    public String meshShape; // triangleMeshShape, convexHullShape
    public boolean isKinematic;  //  "isStatic" ?
    public boolean isPickable;
    public boolean isShadowed;
    public boolean isSteerable;
    public boolean isCharacter;
}

