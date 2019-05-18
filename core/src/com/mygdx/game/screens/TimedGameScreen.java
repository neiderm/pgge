package com.mygdx.game.screens;


import com.badlogic.gdx.Screen;
import com.mygdx.game.SceneLoader;


abstract class TimedGameScreen implements Screen {

    static final int DEFAULT_SCREEN_TIME = 15 * 60 ; // FPS

    int screenTimer = DEFAULT_SCREEN_TIME;

    // private // tmp
    SceneLoader sceneLoader;

    TimedGameScreen(){

        this.sceneLoader = new SceneLoader();
    }

    /** Called when this screen should release all resources. */
    public void dispose (){

        //  screens that load assets must calls assetLoader.dispose() !
        if (null != sceneLoader) {
            sceneLoader.dispose();
            sceneLoader = null;
        }
    }
 }
