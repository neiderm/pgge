package com.mygdx.game.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.SceneLoader;
import com.mygdx.game.util.PrimitivesBuilder;

/**
 * Created by utf1247 on 2/28/2018.
 * Reference:
 *  http://bioboblog.blogspot.com/
 *  http://www.pixnbgames.com/blog/libgdx/how-to-manage-screens-in-libgdx/
 *
 *  Keep this thin .... can it be eliminated?
 */

public class GameWorld implements Disposable {

    private Game game;

    static SceneLoader sceneLoader; // can this be non-static?

    private static GameWorld instance;

//    private GameWorld() {
//    }

    public void initialize(Game game){

        this.game = game;
        PrimitivesBuilder.init();            // one time only .. for now i guess
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

    private String playerObjectName;

    void setPlayerObjectName(String playerObjectName){
        this.playerObjectName = playerObjectName;
    }
    public String getPlayerObjectName(){
        return playerObjectName;
    }

    public void showScreen(Screen screen) {

        game.setScreen(screen); // calls screen.hide() on the current screen
    }


    /* I don't think we see a dispose event on Android */
@Override
    public void dispose() {

        game.getScreen().dispose();

        PrimitivesBuilder.dispose(); // hack, call static method

        instance = null;
    }
}
