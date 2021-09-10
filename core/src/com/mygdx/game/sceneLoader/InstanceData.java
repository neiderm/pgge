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
package com.mygdx.game.sceneLoader;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.features.FeatureAdaptor;

public class InstanceData {

    @SuppressWarnings("unused")
    public InstanceData() { // mt
    }

    public InstanceData(FeatureAdaptor adaptr, Vector3 translation) {

        this.translation = new Vector3(translation);
        this.adaptr = adaptr;
    }

    public InstanceData(Vector3 translation) {

        this.translation = new Vector3(translation);
    }

    public InstanceData(Vector3 translation, Quaternion rotation) {

        this(translation);
        this.rotation = new Quaternion(rotation);
    }

    public Quaternion rotation;
    public Vector3 translation;
    public Color color;

    public FeatureAdaptor adaptr;
}
