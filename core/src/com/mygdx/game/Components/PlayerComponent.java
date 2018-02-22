package com.mygdx.game.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by mango on 1/23/18.
 */

public class PlayerComponent implements Component {

    public float mass = 5.0f;
    public Vector2 inpVect = new Vector2(0, 0); // control input vector

    public Vector3 down = new Vector3();

    public PlayerComponent(float mass) {
        this.mass = mass;
    }
}
