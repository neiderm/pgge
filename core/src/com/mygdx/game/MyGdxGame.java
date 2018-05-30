package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.mygdx.game.screens.GameWorld;

public class MyGdxGame extends Game {


    @Override
    public void create() {

        GameWorld.getInstance().initialize(this);
    }

    @Override
    public void dispose() {
        // ALT+F4, or Android on "back" button calls pause() then dispose() ... but there is still an instance in the background, and when resumed it does NOT cause resume ?????????? (not sure, seems debugger separates, so we will need to observe by adb logging!)
//        super.dispose();   // calls screen->hide()

        GameWorld.getInstance().destroy();  // destroy the current screen
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
