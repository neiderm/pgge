package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;

/**
 * Created by neiderm on 7/5/2018.
 */

public class StatusComponent implements Component {

    private static final int LIFECLOCKDEFAULT = 1;

    public int lifeClock = LIFECLOCKDEFAULT;
    public int prizeCount;
    public int bounty; // is both the players points loot as well as point value of a prize or
    //  killed-thing (added to players loot of course)
    public boolean canExit;
    public int deleteFlag;
    public int[] damage = new int[4];

    public StatusComponent() {
    }

    public StatusComponent(int count) {

        this.lifeClock = count;
    }
}
