package com.mygdx.game.controllers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by utf1247 on 6/25/2018.
 */

public abstract class ICharacterControlAuto {

    public void update(float delta){}

    /*
    this would change to something generic like Object
     */
    public void inputSet(Object o){}

// tmp?
    public void calcSteeringOutput(Vector3 linear, float angular) {}
}
