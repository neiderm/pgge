package com.mygdx.game.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Game;
import com.mygdx.game.Components.PlayerComponent;
import com.mygdx.game.SceneLoader;

/**
 * Created by utf1247 on 2/28/2018.
 */

public class GameWorld {

    public Game game;
    public Engine engine = new Engine();

//    private static SceneLoader sceneloader; // tmp

    public GameWorld(Game game) {

        SceneLoader.init(); // sceneloader = SceneLoader.init();

        this.game = game;
        game.setScreen(new MainMenuScreen(this));
    }

    private ImmutableArray<Entity> entities;

    public void update() {

        entities = engine.getEntitiesFor(Family.all(PlayerComponent.class).get()); // TODO: tmp hack

        if (null == entities)
            entities = null; // wtf

        // this all such a frikkin hack
        boolean playerIsMorte = false;

        for (Entity e : entities) {
            PlayerComponent pc = e.getComponent(PlayerComponent.class);
            if (null != pc) {
                if (pc.died) {
                    playerIsMorte = true;
                    pc.died = false;
                }
            }
        }
        if (playerIsMorte) {
            game.setScreen(new MainMenuScreen(this));
        }
    }

// hack ...................... only for sceneLoader.dispose()
    public void destroy() {
        game.getScreen().dispose();

        SceneLoader.dispose();
    }
}
