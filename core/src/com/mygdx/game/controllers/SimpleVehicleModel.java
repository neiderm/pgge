package com.mygdx.game.controllers;

/*
 * base class for handing control model to libGDX Steerable
 */
public interface SimpleVehicleModel {

    void updateControls(float linear, float angular, boolean flag, float time);
}
