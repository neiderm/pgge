package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;

/**
 * Created by neiderm on 7/5/2018.
 */

public class StatusComponent implements Component {

//    public String name; // tmp??????

    private static final int FPS = 60;
    private static final int LIFECLOCKDEFAULT = 999 * FPS;
    private static final int DIECLOCKDEFAULT = 10 * FPS;

    public int lifeClock = LIFECLOCKDEFAULT;
    public int dieClock = DIECLOCKDEFAULT;

    // hackme: all should be removeable EXCEPT player
    public boolean isEntityRemoveable = true;
    public boolean deleteMe;

    public StatusComponent() {

        isEntityRemoveable = false; // tmp workaround for player
    }

    public StatusComponent(boolean deleteMe){
        this.deleteMe = deleteMe;
    }

    public StatusComponent(int lifeClockSecs, int dieClockSecs) {
        this();
        this.lifeClock = lifeClockSecs * FPS;
        this.dieClock = dieClockSecs * FPS;
    }
}
