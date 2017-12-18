package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.mygdx.game.screens.MainMenuScreen;

public class MyGdxGame extends ApplicationAdapter {

	Screen screen;

	@Override
	public void create () {
		setScreen(new MainMenuScreen(this));
	}

	// GN: copied from "com.badlogic.gdx.Game" ....
	public void setScreen(Screen screen) {

		if (this.screen != null) {
			this.screen.hide();
//            this.screen.dispose(); // .... except for this ?
		}

		this.screen = screen;

		if (this.screen != null) {

			this.screen.show();
			this.screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		} else {

		}
	}

	@Override
	public void render () {

		final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());
		screen.render(delta);
	}
	
	@Override
	public void dispose () {
		screen.dispose();
	}
}
