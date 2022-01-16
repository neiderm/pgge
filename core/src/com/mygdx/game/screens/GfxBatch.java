/*
 * Copyright (c) 2021-2022 Glenn Neidermeier
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
package com.mygdx.game.screens;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class GfxBatch implements Disposable {

    // going around the entity system by stashing references to model instances of graphics that are
    // supposed to be debugging only,
    private static Array<ModelInstance> debugGraphics;

    private final ModelBatch modelBatch;

    private Environment environment;
    private PerspectiveCamera cam;

    private GfxBatch() {
        this.modelBatch = new ModelBatch();
        debugGraphics = new Array<>();
    }

    GfxBatch(Environment environment, PerspectiveCamera cam) {
        this();
        this.environment = environment;
        this.cam = cam;
    }

    public static void draw(ModelInstance modelInstance){
        debugGraphics.add(modelInstance);
    }

    public void update(float deltaTime) {

        modelBatch.begin(cam);

        for (ModelInstance modelInst : debugGraphics) {
            modelBatch.render(modelInst, environment);
        }

        debugGraphics.clear();

        modelBatch.end();
    }


    public void dispose() {
        modelBatch.dispose();
    }
}
