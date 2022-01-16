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
package com.mygdx.game.sceneloader;

import com.badlogic.ashley.core.Entity;

public class GameFeature {

    public int userData;

    private String sObjectName;
    private Entity entity;

    @SuppressWarnings("unused")
    GameFeature() {
        // required for loading from json
    }

    GameFeature(String strName) {
        setObjectName(strName);
    }

    public void setObjectName(String objName) {
        this.sObjectName = objName;
    }

    public String getObjectName() {
        return this.sObjectName;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return this.entity;
    }
}
