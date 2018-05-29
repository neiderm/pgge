package com.mygdx.game.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Game;
import com.mygdx.game.SceneLoader;

/**
 * Created by utf1247 on 2/28/2018.
 */

public class GameWorld {

    private Game game;
    public Engine engine = new Engine();


    public GameWorld(Game game) {

        SceneLoader.init(); // sceneloader = SceneLoader.init();

        this.game = game;
        game.setScreen(new MainMenuScreen(this));
    }


    public void update() {
        game.setScreen(new MainMenuScreen(this));
    }

// hack ...................... only for sceneLoader.dispose()
    public void destroy() {
        game.getScreen().dispose();

        SceneLoader.dispose();
    }
}
