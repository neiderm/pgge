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

import com.badlogic.gdx.graphics.g3d.Model;
import com.mygdx.game.animations.AnimAdapter;
import com.mygdx.game.util.PrimitivesBuilder;

public class ModelInfo {

    public String fileName;
    public Model model;

    AnimAdapter animAdapter;

    @SuppressWarnings("unused")
    public ModelInfo() {
    }

    @SuppressWarnings("unused")
    public ModelInfo(String fileName) {
        this.fileName = fileName;
        this.model = PrimitivesBuilder.getModel();
    }
}
