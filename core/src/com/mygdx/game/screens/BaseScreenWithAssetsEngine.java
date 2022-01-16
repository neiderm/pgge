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

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.GameWorld;
import com.mygdx.game.sceneloader.ModelInfo;
import com.mygdx.game.sceneloader.SceneData;
import com.mygdx.game.sceneloader.SceneLoader;
import com.mygdx.game.systems.RenderSystem;

abstract class BaseScreenWithAssetsEngine implements Screen {

    protected Engine engine;

    private GfxBatch gfxBatch;
    private RenderSystem renderSystem; // for invoking removeSystem (dispose)

    // Scene Loader construction right away because it kicks off new Asset Manager (background) loading process
    private SceneLoader sceneLoader = GameWorld.getInstance().newSceneLoader();

    // field of view should be in the range of 60 to 70 degrees
    PerspectiveCamera cam =
            new PerspectiveCamera(67, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
    // handle to audio track, if loaded
    Music music;

    /*
     * Initialize Engine and Scene Loader
     */
    public void init() {
        // been using same light setup as ever
        //  https://xoppa.github.io/blog/loading-a-scene-with-libgdx/
        // shadow lighting lifted from 'Learning_LibGDX_Game_Development_2nd_Edition' Ch. 14 example
        Environment environment = new Environment();
        environment.set(
                new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));

        DirectionalShadowLight shadowLight = new DirectionalShadowLight(1024, 1024, 120, 120, 1f, 300);
        shadowLight.set(0.8f, 0.8f, 0.8f, new Vector3(0.5f, -1.0f, 0f));
        environment.add(shadowLight);
        environment.shadowMap = shadowLight;
        gfxBatch = new GfxBatch(environment, cam);

        engine = new Engine();
        SceneLoader.buildScene(engine);

        renderSystem = new RenderSystem(shadowLight, environment, cam);
        engine.addSystem(renderSystem);

        // point the camera to platform
        final Vector3 camPosition = new Vector3(0, 1.2f, 3.2f);
        final Vector3 camLookAt = new Vector3(0, 0, 0);

        cam.position.set(camPosition);
        cam.lookAt(camLookAt);
        cam.up.set(0, 1, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();
        // load audio track
        final String AUDIO_TRACK = "Audio_Track_0";
        music = loadAudioTrack(AUDIO_TRACK);
        Gdx.app.log(Class.class.toString(), "music = " + music);
    }

    Music loadAudioTrack(String trkname) {
        // load audio track
        Music track = null;
        SceneData sd = GameWorld.getInstance().getSceneData();

        if (null != sd) {
            ModelInfo mi = sd.modelInfo.get(trkname);
            if (null != mi) {
                String audioTrack = mi.fileName;
                if (null != audioTrack) {
                    track = GameWorld.AudioManager.getMusic(audioTrack);
                }
            }
        }
        return track;
    }

    @Override
    public void render(float deltaTime) {

        Gdx.gl.glViewport(0, 0, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
        Gdx.gl.glClearColor(0, 0, 0, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        engine.update(deltaTime);
        gfxBatch.update(deltaTime);
    }

    @Override
    public void dispose() {

        engine.removeSystem(renderSystem); // make the system dispose its stuff
        gfxBatch.dispose();
        //  screens that load assets must calls assetLoader.dispose() !
        if (null != sceneLoader) {
            sceneLoader.dispose();
            sceneLoader = null;
        }
    }
}
