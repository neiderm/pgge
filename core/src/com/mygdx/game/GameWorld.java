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
import com.mygdx.game.screens.SplashScreen;
import com.mygdx.game.util.PrimitivesBuilder;

/**
 * Created by neiderm on 2/28/2018.
 * Reference for "ScreenManager" arch. pattern:
 *   http://bioboblog.blogspot.com/2012/08/libgdx-screen-management.html (posted 16.Aug.2012)
 * Based on libgdx-screen-management, but each Screen is a new instance and showScreen() has Object
 * parameter (extends Stage implements Screen ???? )
 *   http://www.pixnbgames.com/blog/libgdx/how-to-manage-screens-in-libgdx/ (Posted 13.Nov.2014, improved to use enum)
 */
public final class GameWorld implements Disposable {

    public static final int VIRTUAL_WIDTH = Gdx.graphics.getWidth();
    public static final int VIRTUAL_HEIGHT = Gdx.graphics.getHeight();

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

    void initialize(Game game) {

        this.game = game;

        // static subsystems initialized only once per application run
        Bullet.init();
        PrimitivesBuilder.init();            // one time only .. for now i guess

        game.setScreen(new SplashScreen());
    }

    /*
     * any screen that has more than trivial setup should be deferred thru the loading screen!
     */
    public void showScreen(Screen screen  /* ScreenEnum screenEnum, Object... params */) {

        if (null == game) {
            return;
        }
        game.setScreen(screen); // calls screen.hide() on the current screen but should that call dispose() ??????
    }

    /*
     please don't hate the Game World globals needed to be shared between Game World and Game Screen
     */
    private int controllerMode;

    public int getControllerMode() {
        return controllerMode;
    }

    public void setControllerMode(int iMode) {
        this.controllerMode = iMode;
    }

    private boolean isPaused = false;

    public boolean getIsPaused() {

        return isPaused;
    }

    public void setIsPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    private boolean isTouchScreen = false;

    public boolean getIsTouchScreen() {
        return isTouchScreen;
    }

    public void setIsTouchScreen(boolean isTouchScreen) {
        this.isTouchScreen = isTouchScreen;
    }

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
    public void setSceneData(String path) {

        setSceneData(path, null);
    }

    /*
     passes along the player object name and path to next screen json
     */
    public void setSceneData(String path, String playerObjectName) {

        this.sceneDataFile = path; // keep this for screen restart reloading

        ModelInfo selectedModelInfo = null;
        // BEFORE the scene data is reloaded, ....
        if (null != playerObjectName) {
            // get the  player model info from previous scene data
            selectedModelInfo = this.sceneData.modelInfo.get(playerObjectName);
        }

        this.sceneData = SceneData.loadData(path, playerObjectName);

        if (null != selectedModelInfo) {
            // set the player object model info in new scene data isntance
            sceneData.modelInfo.put(playerObjectName, selectedModelInfo );
        }
    }

    /*
     * this is only for Select Screen, to set the "tag" on the player object name
     * bah duplicated code
     */
    public void loadSceneData(String path, String playerObjectName) {

        this.sceneDataFile = path; // keep this for screen restart reloading

        ModelInfo selectedModelInfo = null;
        // BEFORE the scene data is reloaded, ....
        if (null != playerObjectName) {
            // get the  player model info from previous scene data
            selectedModelInfo = this.sceneData.modelInfo.get(playerObjectName);
        }

        // When loading from Select Screen, need to distinguish the name of the selected player 
        // object by an arbitrary character string to make sure locally added player model info 
        // doesn't bump into the user-designated model info sections in the screen json files
        String PLAYER_OBJECT_TAG = "P0_";
        String adjPlayerObjectName = PLAYER_OBJECT_TAG + playerObjectName;

        this.sceneData = SceneData.loadData(path, adjPlayerObjectName);

        sceneData.modelInfo.put(adjPlayerObjectName, selectedModelInfo );
    }

    /*
     * for screen reload/restart only .. assume data file is already set by previous caller
     */
    public void reloadSceneData(String modelName) {
        setSceneData(sceneDataFile, modelName);
    }

    public SceneData getSceneData() {
        return sceneData;
    }

    public GameFeature getFeature(String featureName) {
        return sceneData.features.get(featureName);
    }

    public void addSpawner(GameObject object) {
        addSpawner(object, ModelGroup.MGRP_DEFAULT_MDL_NAME);
    }

    private static final String _CLASS_ = "GameWorld";

    private void addSpawner(GameObject object, String modelName) {

        ModelGroup mg = sceneData.modelGroups.get(ModelGroup.SPAWNERS_MGRP_KEY);
        /*
         * this is likely jacked up, should it be possible for multiple calls into addSPawner() would result
         * in additional objects queued into that MG instance, but the MG
         */
        if (null != mg) {
            Gdx.app.log(_CLASS_, "spawners mg NOT null ... ?");

            // model Name is used for e.g. "tank" and similiar models - loading entire model and globbing it toether (game object is literally "*" )
            if (mg.getModelName().isEmpty() || mg.getModelName().equals(ModelGroup.MGRP_DEFAULT_MDL_NAME)) {
                Gdx.app.log(_CLASS_, "mg.modelName.isEmpty(), ok to add another Game object to this model Group ... ?");
            }
        } else {
            mg = new ModelGroup(modelName);
            sceneData.modelGroups.put(ModelGroup.SPAWNERS_MGRP_KEY, mg);
        }

        mg.addElement(object);
    }

    /* I don't think we see a dispose event on Android */
    @Override
    public void dispose() {

        game.getScreen().dispose();

        PrimitivesBuilder.dispose(); //  call static method

        instance = null;
    }
}
