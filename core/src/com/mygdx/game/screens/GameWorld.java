package com.mygdx.game.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.mygdx.game.SceneLoader;

/**
 * Created by utf1247 on 2/28/2018.
 * Reference:
 *  http://bioboblog.blogspot.com/
 *  http://www.pixnbgames.com/blog/libgdx/how-to-manage-screens-in-libgdx/
 */

public class GameWorld {

    private Game game;

    static SceneLoader sceneLoader;

    private static GameWorld instance;

    private GameWorld() {
    }

    public void initialize(Game game){

        this.game = game;
        game.setScreen(new SplashScreen());
    }

    public static GameWorld getInstance(){
        if (null == instance){
            instance = new GameWorld();
        }
        return instance;
    }

/*
 right now this is the only way to signal from the PlayerCharacter:keyDown to GameScreen :(
 */
    private boolean isPaused = false;

    public boolean getIsPaused(){
        return isPaused;
    }
    public void setIsPaused(boolean isPaused){
        this.isPaused = isPaused;
    }

    private boolean isTouchScreen = false;

    public boolean getIsTouchScreen(){
        return isTouchScreen;
    }

    void setIsTouchScreen(boolean isTouchScreen){
        this.isTouchScreen = isTouchScreen;
    }



    public void showScreen(Screen screen) {

        game.setScreen(screen); // calls screen.hide() on the current screen

        // Dispose previous screen ?????????????
//        if (currentScreen != null) {
//            currentScreen.dispose();
//        }
    }

// hack ...................... only for sceneLoader.dispose()
    // Note that Screen:dispose() is not called automatically by framework
    public void dispose() {

        game.getScreen().dispose();

        instance = null;
    }
}
