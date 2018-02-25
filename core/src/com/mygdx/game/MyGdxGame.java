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

	// GN: copied from "com.badlogic.gdx.Game" ....
	@Override
	public void setScreen(Screen screen) {

		if (this.screen != null) {
			this.screen.hide();
//            this.screen.dispose(); // .... except for this ?
		}

		this.screen = screen;

		if (this.screen != null) {

			this.screen.show();
			this.screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		}
	}
}
