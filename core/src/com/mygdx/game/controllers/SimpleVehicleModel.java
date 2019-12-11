package com.mygdx.game.controllers;

import com.badlogic.gdx.math.Vector2;

/*
 * base class for handing control model to libGDX Steerable
 */
public interface SimpleVehicleModel {

    void updateControls(Vector2 v0, Vector2 v1, boolean flag, float time);
}
