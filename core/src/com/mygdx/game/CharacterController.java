package com.mygdx.game;

import com.badlogic.gdx.math.Vector3;

/**
 * Created by mango on 2/10/18.
 */

public interface CharacterController {

    // I don't know that this would really work ;(
//    public Vector3 doControl(Vector3 setpoint, Vector3 process) ;
    void update(float delta);
}
