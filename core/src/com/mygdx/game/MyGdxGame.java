package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.mygdx.game.screens.GameWorld;

public class MyGdxGame extends Game {

    private GameWorld world;

    @Override
    public void create() {
        world = new GameWorld(this);
    }

/*    @Override
    public void render(){
        super.render();
        world.update();
    }*/

    @Override
    public void dispose() {
        // ALT+F4, or possibly "back" button
//        super.dispose();   // calls screen->hide()
        world.destroy();  // hack ... for sceneloader.dispose()
    }
}
