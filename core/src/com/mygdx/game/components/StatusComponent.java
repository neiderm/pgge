package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.mygdx.game.screens.GameUI;

/**
 * Created by neiderm on 7/5/2018.
 */

public class StatusComponent /* "UIComponent" ? */ implements Component {

    private static final int FPS = 60;
    private static final int LIFECLOCKDEFAULT = 0 * FPS;

    public int lifeClock = LIFECLOCKDEFAULT;
    public int prizeCount;
    public int bounty; // is both the players points loot as well as point value of a prize or
                       // killed-thing (added to players loot of course)

    public boolean canExit;
    public boolean deleteMe;
    public int deleteFlag;

    public StatusComponent() {
    }

    public StatusComponent(int lifeClockSecs) {

        this.lifeClock = lifeClockSecs * FPS;
    }
}
