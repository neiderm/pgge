package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;

/**
 * Created by neiderm on 7/5/2018.
 */

public class StatusComponent implements Component {

    private static final int LIFECLOCKDEFAULT = 0;

    public int lifeClock = LIFECLOCKDEFAULT;
    public int prizeCount;
    public int bounty; // is both the players points loot as well as point value of a prize or
    //  killed-thing (added to players loot of course)
    public boolean canExit;
    public boolean deleteMe;
    public int deleteFlag;

    public StatusComponent() {
    }

    public StatusComponent(int count) {

        this.lifeClock = count;
    }

    public StatusComponent(int count, int bounty) {

        this.lifeClock = count;
        this.bounty = bounty;
    }
}
