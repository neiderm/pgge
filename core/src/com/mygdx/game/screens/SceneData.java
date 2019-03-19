package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.mygdx.game.util.PrimitivesBuilder;

import java.util.HashMap;

public class SceneData {

    public static class ModelGroup {
        ModelGroup() {
        }

        public ModelGroup(String groupName) {
        }

        ModelGroup(String groupName, String modelName) {
            this(groupName);
            this.modelName = modelName;
        }

        public String modelName;
        public Array<GameObject> gameObjects = new Array<GameObject>();
    }

    public static class ModelInfo {
        ModelInfo() {
        }

        ModelInfo(String fileName) {
            this.fileName = fileName;
        }

        public String fileName;
        public Model model = PrimitivesBuilder.primitivesModel;  // allow it to be default
    }

    public HashMap<String, ModelGroup> modelGroups = new HashMap<String, ModelGroup>();
    public HashMap<String, ModelInfo> modelInfo = new HashMap<String, ModelInfo>();

    public static class GameObject {
        GameObject() {
        }

        public GameObject(String objectName, String meshShape) {
            this.objectName = objectName;
            this.meshShape = meshShape;
            this.isShadowed = true;
            this.isKinematic = true;
            this.scale = new Vector3(1, 1, 1); // placeholder
        }

        public static class InstanceData {
            InstanceData() {
            }

            InstanceData(Vector3 translation, Quaternion rotation) {
                this.translation = new Vector3(translation);
                this.rotation = new Quaternion(rotation);
                this.color = Color.CORAL;
            }

            public Quaternion rotation;
            public Vector3 translation;
            public Color color;
        }

        public Array<GameObject.InstanceData> instanceData = new Array<GameObject.InstanceData>();
        public String objectName;
        //            Vector3 translation; // needs to be only per-instance
        public Vector3 scale; // NOT per-instance, all instances should be same scale (share same collision Shape)
        public float mass;
        public String meshShape; // triangleMeshShape, convexHullShape
        public boolean isKinematic;  // TODO: change "isStatic"
        public boolean isShadowed;
    }


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
