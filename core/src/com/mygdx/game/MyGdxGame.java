package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.mygdx.game.screens.GameWorld;

public class MyGdxGame extends Game {

/*    @Override
    public void render() {
        super.render(); // I can hook into this render, as well as that of current screen
    }*/

    @Override
    public void create() {
        Gdx.input.setCatchBackKey(true);
        GameWorld.getInstance().initialize(this);
    }

    @Override
    public void dispose() {
        // Make Game World disposable ... Note this only seems to be incurred in Desktop lwjgl environment (Alt+F4).
        // ( btw Screen:dispose() is not called automatically by framework )
        GameWorld.getInstance().dispose();  // dispose the current screen
    }


/*    @Override
    public void pause () {
        super.pause(); // .................... android center or "back" btn
    }

    // Note: android -  when resume into game screen, there are 2 gl surface changed events, so 2 series of resize+resume

    // from android "resume" (2)  (left soft button)
    @Override
    public void resume () {

        super.resume();
    }

    // from android "resume" (1)  (left soft button)
    @Override
    public void resize (int width, int height) {
        super.resize(width, height);
    }*/
}
