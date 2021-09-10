package com.mygdx.game.controllers;

/*
 * base class for handing control model to libGDX Steerable
 */
public interface ControllerAbstraction {

    // interfacing between client input abstraction and controller
    int SW_FIRE1 = 0;
    int SW_FIRE2 = 1;
    int SW_SQUARE = 2;
    int SW_TRIANGL = 3;

    void updateControls(float[] analogs, boolean[] switches, float time);
}
