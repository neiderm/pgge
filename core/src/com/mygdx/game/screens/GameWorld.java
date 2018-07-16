package com.mygdx.game.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.mygdx.game.SceneLoader;

/**
 * Created by utf1247 on 2/28/2018.
 * Reference:
 *  http://bioboblog.blogspot.com/
 *  http://www.pixnbgames.com/blog/libgdx/how-to-manage-screens-in-libgdx/
 */

public class GameWorld {

    private Game game;
    public static AssetManager assets;
    //    public Engine engine = new Engine();

    private static GameWorld instance;

    private GameWorld() {
    }

    public void initialize(Game game){

        assets = SceneLoader.init();

        this.game = game;
        game.setScreen(new SplashScreen()); // game.setScreen(new MainMenuScreen());
    }

    public static GameWorld getInstance(){
        if (null == instance){
            instance = new GameWorld();
        }
        return instance;
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

        SceneLoader.dispose();

        instance = null;
    }
}
