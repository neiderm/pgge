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
package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.sceneLoader.GameFeature;
import com.mygdx.game.sceneLoader.GameObject;
import com.mygdx.game.sceneLoader.ModelGroup;
import com.mygdx.game.sceneLoader.ModelInfo;
import com.mygdx.game.sceneLoader.SceneData;
import com.mygdx.game.screens.LoadingScreen;
import com.mygdx.game.screens.ReduxScreen;
import com.mygdx.game.util.PrimitivesBuilder;

/**
 * Created by neiderm on 2/28/2018.
 * Reference for "ScreenManager" arch. pattern:
 * http://bioboblog.blogspot.com/2012/08/libgdx-screen-management.html (posted 16.Aug.2012)
 * Based on libgdx-screen-management, but each Screen is a new instance and showScreen() has Object
 * parameter (extends Stage implements Screen ???? )
 * http://www.pixnbgames.com/blog/libgdx/how-to-manage-screens-in-libgdx/ (Posted 13.Nov.2014, improved to use enum)
 */
public final class GameWorld implements Disposable {

    private static final String CLASS_STRING = "GameWorld";
    private static final String DEFAULT_SCREEN = "SelectScreen.json";
    // deserves a more unique name (in json too)
    public static final String LOCAL_PLAYER_FNAME = "Player";
    // default font from gdx-skins
    public static final String DEFAULT_FONT_FNT = "data/default.fnt";
    public static final String DEFAULT_FONT_PNG = "data/default.png";

    //  for working with ViewPort?
    public static final int SCREEN_WIDTH = Gdx.graphics.getWidth();
    public static final int SCREEN_HEIGHT = Gdx.graphics.getHeight();

    // font is sized for libGdx default desktop screen size
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;
    // Pixel 2
    public static final int VIRTUAL_WIDTH = 1794;
    public static final int VIRTUAL_HEIGHT = 1080;

    public static final float FONT_X_SCALE = ((float) VIRTUAL_WIDTH / DEFAULT_WIDTH); // 2.80f;
    public static final float FONT_Y_SCALE = ((float) VIRTUAL_HEIGHT / DEFAULT_HEIGHT); // 2.125


    public enum GAME_STATE_T {
        ROUND_NONE,
        ROUND_ACTIVE,
        ROUND_OVER_RESTART,
        ROUND_OVER_QUIT,
        ROUND_OVER_TIMEOUT,
        ROUND_OVER_MORTE,    // out of time or dead ... continue?
        ROUND_COMPLETE_WAIT, // @ completing the goal .. short pause (for effect)
        ROUND_COMPLETE_NEXT, // transition to next screen after arena complete
        ROUND_ACTIVATE_ON_ALL // dummy state for "unconditionally" activating features
    }

    private static GameWorld instance;

    private SceneData sceneData;
    private String sceneDataFile;
    private Game game;

    // created lazily and cached for later usage.
    public static GameWorld getInstance() {
        if (null == instance) {
            instance = new GameWorld();
        }
        return instance;
    }

    public static void setInstance(GameWorld instance) {
        GameWorld.instance = instance;
    }

    void initialize(Game game) {
        this.game = game;
        // static subsystems initialized only once per application run
        Bullet.init();
        PrimitivesBuilder.init();

        showScreen(); // show loading/splash screen
    }

    /*
     * any screen that has more than trivial setup should be deferred thru the loading screen!
     */
    public void showScreen() {
        if (Gdx.files.internal(DEFAULT_SCREEN).exists()) {

            getInstance().setSceneData(DEFAULT_SCREEN);
            getInstance().showScreen(new LoadingScreen(true, LoadingScreen.ScreenTypes.SETUP));
        } else {
            Gdx.app.log("SplashScreen",
                    "Select Screen data not found, loading Test Screen");
            getInstance().showScreen(new ReduxScreen());
        }
    }

    public void showScreen(Screen screen) {
        if (null == game) {
            return;
        }
        game.setScreen(screen); // calls screen.hide() on the current screen
    }

    /*
     * globals that need to be shared between Game World and Screens
     */
    // controller mode
    private int controllerMode;

    public int getControllerMode() {
        return controllerMode;
    }

    public void setControllerMode(int iMode) {
        this.controllerMode = iMode;
    }

    // is paused
    private boolean isPaused = false;

    public boolean getIsPaused() {

        return isPaused;
    }

    public void setIsPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    // is touch screen
    private boolean isTouchScreen = false;

    public boolean getIsTouchScreen() {
        return isTouchScreen;
    }

    public void setIsTouchScreen(boolean isTouchScreen) {
        this.isTouchScreen = isTouchScreen;
    }

    // round active state
    private GAME_STATE_T roundActiveState = GAME_STATE_T.ROUND_NONE; // for better or worse ... ;)  gameScreenState ??

    public GAME_STATE_T getRoundActiveState() {
        return roundActiveState;
    }

    public void setRoundActiveState(GAME_STATE_T state) {
        roundActiveState = state;
    }

    /*
     * set Select Screen data
     */
    public void setSceneData(String fileName) {
        sceneDataFile = fileName; // keep this persistent for screen restart/reloading
        sceneData = SceneData.loadData(sceneDataFile, null);
    }

    /**
     * passes along the player object name and path to next screen json
     *
     * @param fileName         name of json file to load
     * @param playerObjectName if not null, previous scene player object data is reloaded to new screen.
     */
    public void setSceneData(String fileName, String playerObjectName) {

        sceneDataFile = fileName; // keep this persistent for screen restart/reloading

        ModelInfo selectedModelInfo = null;

        if (null != playerObjectName) {
            // get the player model info from previous scene data
            selectedModelInfo = sceneData.modelInfo.get(playerObjectName);
        }
        sceneData = SceneData.loadData(sceneDataFile, playerObjectName);
        // definitely needs to be non-null here!
        if (null != selectedModelInfo) {
            // set the player object model info in new scene data isntance
            sceneData.modelInfo.put(playerObjectName, selectedModelInfo);
        }
    }

    /**
     * only for Select Screen, to set the "tag" on the player object name
     *
     * @param fileName          path to screen json file to load
     * @param playerFeatureName player feature name
     * @param selectedModelInfo player model info from previous screen
     */
    public void setSceneData(String fileName, String playerFeatureName, ModelInfo selectedModelInfo) {

        sceneDataFile = fileName; // keep this persistent for screen restart/reloading
        // now instantiate the new scene data
        sceneData = SceneData.loadData(fileName, playerFeatureName);
//        if (null != selectedModelInfo) ... don't care
        sceneData.modelInfo.put(playerFeatureName, selectedModelInfo);
    }

    /*
     * for screen reload/restart only .. assume data file is already set by previous caller
     */
    public void reloadSceneData(String playerObjectName) {
        setSceneData(sceneDataFile, playerObjectName);
    }

    public SceneData getSceneData() {
        return sceneData;
    }

    /**
     * Retrieves the requested Feature by name thru Scene Loader
     *
     * @param featureName feature name
     * @return Game Feature
     */
    public GameFeature getFeature(String featureName) {
        return sceneData.features.get(featureName);
    }

    public void addSpawner(GameObject object) {
        addSpawner(object, ModelGroup.MGRP_DEFAULT_MDL_NAME);
    }

    private void addSpawner(GameObject object, String modelName) {

        ModelGroup mg = sceneData.modelGroups.get(ModelGroup.SPAWNERS_MGRP_KEY);
        /*
         * this is likely jacked up, should it be possible for multiple calls into addSPawner() would result
         * in additional objects queued into that MG instance, but the MG
         */
        if (null != mg) {
            Gdx.app.log(CLASS_STRING, "spawners ModelGroup != null");
        } else {
            mg = new ModelGroup(modelName);
            sceneData.modelGroups.put(ModelGroup.SPAWNERS_MGRP_KEY, mg);
        }
        mg.addElement(object);
    }

    @Override
    public void dispose() {
        game.getScreen().dispose();
        PrimitivesBuilder.dispose(); //  call static method
        setInstance(null);
    }
}
