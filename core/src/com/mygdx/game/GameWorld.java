package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.screens.SplashScreen;
import com.mygdx.game.util.PrimitivesBuilder;

/**
 * Created by neiderm on 2/28/2018.
 * Reference for "ScreenManager" arch. pattern:
 *  http://bioboblog.blogspot.com/2012/08/libgdx-screen-management.html (posted 16.Aug.2012)
 * Based on libgdx-screen-management, but each Screen is a new instance and showScreen() has Object parameter (extends Stage implements Screen ???? )
 *  http://www.pixnbgames.com/blog/libgdx/how-to-manage-screens-in-libgdx/ (Posted 13.Nov.2014, improved to use enum)
 *
 *  intented as singletonKeep, keep this thin
 */

public final class GameWorld implements Disposable {

    public static final int VIRTUAL_WIDTH =  Gdx.graphics.getWidth(); //tmp ..  640;
    public static final int VIRTUAL_HEIGHT = Gdx.graphics.getHeight(); // tmp .. 480;

    private Game game;

    public static SceneLoader sceneLoader; // can this be non-static?

    private static GameWorld instance;

    private GameWorld() {
    }

    // created lazily and cached for later usage.
    public static GameWorld getInstance(){
        if (null == instance){
            instance = new GameWorld();
        }
        return instance;
    }

    void initialize(Game game){

        this.game = game;

        // static subsystems initialized only once per application run
        Bullet.init();
        PrimitivesBuilder.init();            // one time only .. for now i guess

        game.setScreen(new SplashScreen()); // can be done here or by caller ?
    }

    /*
     * is caller calling this on their render() ? maybe it doesnt matter.
     * should be concerned about how much construction is done then?
     * any screen that has more than trivial setup should be deferred thru the loading screen!
     *
     * mostly I do all screen init in constructor and do nothing in show() and chain the dispose() to hide() ... but we could call dispose below ....
     * is there such a thing as leaving a screen instance persist (like a single instance of GameScreen  that could hold
     * persistent data. It would still do model dispose etc. on the hide() event and then rebuild those on show()
     */
    public void showScreen(Screen screen  /* ScreenEnum screenEnum, Object... params */ ) {

        if (null == game)
            return;

        // Get current screen to dispose it >>>>>  ???????
        Screen currentScreen = game.getScreen();

        // Show new screen
//        AbstractScreen newScreen = screenEnum.getScreen(params);
//        newScreen.buildStage();

        game.setScreen(screen); // calls screen.hide() on the current screen but should that call dispose() ??????

        // Dispose previous screen
//        if (currentScreen != null) {
//            currentScreen.dispose();
//        }
    }

    /*
     right now this is the only way to signal from the PlayerCharacter:keyDown to GameScreen :(
     */
    private boolean isPaused = false;

    public boolean getIsPaused(){
        return isPaused;
    }
    public void setIsPaused(boolean isPaused){
        this.isPaused = isPaused;
    }


    private boolean isTouchScreen = false;

    public boolean getIsTouchScreen(){
        return isTouchScreen;
    }

    public void setIsTouchScreen(boolean isTouchScreen){
        this.isTouchScreen = isTouchScreen;
    }


    private String playerObjectName;

    public void setPlayerObjectName(String playerObjectName){
        this.playerObjectName = playerObjectName;
    }

    public String getPlayerObjectName(){
        return playerObjectName;
    }

    /* I don't think we see a dispose event on Android */
@Override
    public void dispose() {

        game.getScreen().dispose();

        PrimitivesBuilder.dispose(); // hack, call static method
//        BulletWorld.getInstance().dispose();

        instance = null;
    }
}
