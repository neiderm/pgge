package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;

/**
 * Created by neiderm on 7/5/2018.
 */

public class StatusComponent implements Component {

    public final int[] damage = new int[4];
    // Bounty is both the players points loot as well as point value of a prize or
    //  killed-thing (added to players loot of course)
    public int bounty;
    public int deleteFlag;
    public int lifeClock = LIFECLOCKDEFAULT;
    public int prizeCount;
    public boolean canExit;

    private static final int LIFECLOCKDEFAULT = 1;

    public StatusComponent() {
    }

    public StatusComponent(int count) {
        this.lifeClock = count;
    }
}
