package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.GameWorld;
import com.mygdx.game.MyGdxGame;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        // libGdx default for desktop is 640x480 ... 1.3333 AR
//		config.width = 640;
//		config.height = 480;
//      // 1.666666667
//		// HTC Droid Incredible 2, Samsung Galaxy Prime
//		config.width = 800;
//		config.height = 480;
//		// 1.7777
//      // HTC One M8
//      config.width = 1920;
//      config.height = 1080;
//
//		config.width = 1280;
//		config.height = 720;
//
//		config.width = 960;
//		config.height = 540;

        config.width = GameWorld.VIRTUAL_WIDTH;
        config.height = GameWorld.VIRTUAL_WIDTH;

        new LwjglApplication(new MyGdxGame(), config);
    }
}
