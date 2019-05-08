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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.HashMap;

public class SceneData {

    public HashMap<String, GameFeature> features = new HashMap<String, GameFeature>();
    public HashMap<String, ModelGroup> modelGroups = new HashMap<String, ModelGroup>();
    public HashMap<String, ModelInfo> modelInfo = new HashMap<String, ModelInfo>();


    public static void saveData(SceneData data) {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json); // see "https://github.com/libgdx/libgdx/wiki/Reading-and-writing-JSON"
        FileHandle fileHandle = Gdx.files.local("GameData_out.json");
        if (data != null) {
//            fileHandle.writeString(Base64Coder.encodeString(json.prettyPrint(gameData)), false);
            fileHandle.writeString(json.prettyPrint(data), false);
            //System.out.println(json.prettyPrint(gameData));
        }
    }

    public static SceneData loadData(String path) {
        Json json = new Json();
        FileHandle fileHandle = Gdx.files.internal(path);
        //        gameData = json.fromJson(sceneData.class, Base64Coder.decodeString(fileHandle.readString()));
        return json.fromJson(SceneData.class, fileHandle.readString());
    }
}
