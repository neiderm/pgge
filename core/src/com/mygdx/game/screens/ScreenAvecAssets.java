package com.mygdx.game.screens;


import com.badlogic.gdx.Screen;
import com.mygdx.game.SceneLoader;


abstract class ScreenAvecAssets implements Screen {

    // private // tmp
    SceneLoader sceneLoader;

    ScreenAvecAssets(){

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
