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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.mygdx.game.GameWorld;

import java.util.HashMap;
import java.util.Map;

/*
 * merge into GameWorld?
 References
 *  http://niklasnson.com/programming/network/tips%20and%20tricks/2017/09/15/libgdx-save-and-load-game-data.html
 */
public class SceneData {
    public final HashMap<String, GameFeature> features = new HashMap<>();
    public final HashMap<String, ModelGroup> modelGroups = new HashMap<>();
    public final HashMap<String, ModelInfo> modelInfo = new HashMap<>();

    /*
     * Reference:
     * "https://github.com/libgdx/libgdx/wiki/Reading-and-writing-JSON"
     */
    private static void saveData(SceneData data, String fileName) {

        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);

        if (data != null) {
            FileHandle fileHandle = Gdx.files.local(fileName);
            fileHandle.writeString(json.prettyPrint(data), false);
        }
    }

    public static SceneData loadData(String path, String playerObjectName) {

        Json json = new Json();
        FileHandle fileHandle = Gdx.files.internal(path);
        // if null != fileHandle
        SceneData screenSdata = json.fromJson(SceneData.class, fileHandle.readString());

        // save data to file while the state of the scene data is still pristine
        saveData(screenSdata, "GameData_out.json");

        // load the default assets for UI and other things used throughout the entire game
        fileHandle = Gdx.files.internal("GameData.json");
        // if null != fileHandle
        SceneData baseSdata = json.fromJson(SceneData.class, fileHandle.readString());

        // merge the features/model groups with the data set of the new screen
        if (null != baseSdata) {

            HashMap<String, ModelInfo> info = baseSdata.modelInfo;

            for (Map.Entry<String, ModelInfo> entry : info.entrySet()) {
                String key = entry.getKey();
                screenSdata.modelInfo.put(key, info.get(key));
            }

            HashMap<String, ModelGroup> mgrps = baseSdata.modelGroups;

            for (Map.Entry<String, ModelGroup> entry : mgrps.entrySet()) {
                String key = entry.getKey();
                screenSdata.modelGroups.put(key, mgrps.get(key));
            }
        }

        // localplayer object-name is passed along from parent screen ... make a Game Feature in which
        // to stash this "persistent" local player info. Other systems/comps will be looking for this
        // magik name to get reference to the entity.
        GameFeature gf = screenSdata.features.get(GameWorld.LOCAL_PLAYER_FNAME);

        if (null != gf) {
            // Allow local player game feature to be defined in JSON (user Data)
            gf.setObjectName(playerObjectName);
        } else {
            gf = new GameFeature(playerObjectName);
            screenSdata.features.put(GameWorld.LOCAL_PLAYER_FNAME, gf);
        }

        return screenSdata;
    }
}
