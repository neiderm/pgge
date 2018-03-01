package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.mygdx.game.screens.MainMenuScreen;

public class MyGdxGame extends Game {

	@Override
	public void create () {

	    setScreen(new MainMenuScreen(this));
	}

    @Override
    public void dispose() {
        this.screen.dispose();
    }
}
