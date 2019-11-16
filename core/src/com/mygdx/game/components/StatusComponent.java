package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.game.screens.GameUI;

/**
 * Created by neiderm on 7/5/2018.
 */

public class StatusComponent /* "UIComponent" ? */ implements Component {

    public GameUI UI; // UI is realized as instance of Stage ... tmp need to use GameUI until figure out the interface

    private static final int FPS = 60;
    private static final int LIFECLOCKDEFAULT = 999 * FPS;

    public int lifeClock = LIFECLOCKDEFAULT;

    public boolean deleteMe;
    public int deleteFlag;

    public StatusComponent() {
    }

    public StatusComponent(boolean deleteMe){
        this.deleteMe = deleteMe;
//        this.lifeClock = 0 * FPS;
    }

    public StatusComponent(Stage UI, int lifeClockSecs, int dieClockSecs) {
        this.UI = (GameUI)UI; // tmp need to use GameUI until
        this.lifeClock = lifeClockSecs * FPS;
    }
}
