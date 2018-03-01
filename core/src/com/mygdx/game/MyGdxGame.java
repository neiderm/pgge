package com.mygdx.game;

import com.badlogic.gdx.Game;

public class MyGdxGame extends Game {

    private GameWorld world;

	@Override
	public void create () {
        world = new GameWorld(this);
	}

    @Override
    public void render(){
        super.render();
        world.update();
    }

    @Override
    public void dispose() {
        world.destroy();
    }
}
