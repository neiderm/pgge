package com.mygdx.game.screens;


import com.badlogic.gdx.Screen;
import com.mygdx.game.SceneLoader;


abstract class ScreenAvecAssets implements Screen {

    // private // tmp
    SceneLoader screenData;

    ScreenAvecAssets(SceneLoader data){

        this.screenData = data;
    }

    /** Called when this screen should release all resources. */
    public void dispose (){

        //  screens that load assets must calls assetLoader.dispose() !
        if (null != screenData) {
            screenData.dispose();
            screenData = null;
        }
    }
 }
