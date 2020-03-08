package com.mygdx.game.controllers;

/*
 * base class for handing control model to libGDX Steerable
 */
public interface ControllerAbstraction {

    // iterfacing between client input abstraction and controller
    public static final int  SW_FIRE1 = 0;
    public static final int  SW_FIRE2 = 1;
    public static final int  SW_SQUARE = 2;
    public static final int  SW_TRIANGL = 3;

    void updateControls(float[] analogs, boolean[] switches, float time);
}
