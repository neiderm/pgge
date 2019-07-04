package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.util.IStatusUpdater;

/**
 * Created by neiderm on 7/5/2018.
 */

public class StatusComponent implements Component {

    public String name;

    private static final int FPS = 60;
    private static final int LIFECLOCKDEFAULT = 999 * FPS;
    private static final int DIECLOCKDEFAULT = 10 * FPS;

    public int lifeClock = LIFECLOCKDEFAULT;
    public int dieClock = DIECLOCKDEFAULT;

    // hackme: all should be removeable EXCEPT player
    public boolean isEntityRemoveable = true;

    public IStatusUpdater statusUpdater;


    private StatusComponent() {

        this("no-name");
        isEntityRemoveable = false; // tmp workaround for player
    }

    public StatusComponent(int lifeClockSecs, int dieClockSecs) {
        this();
        this.lifeClock = lifeClockSecs * FPS;
        this.dieClock = dieClockSecs * FPS;
    }

    public StatusComponent(String name) {

        this.name = name;
    }
}
