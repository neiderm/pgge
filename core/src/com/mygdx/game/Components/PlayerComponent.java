package com.mygdx.game.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by mango on 1/23/18.
 */

public class PlayerComponent implements Component {

    public float mass = 5.0f;
    public Vector3 vvv = new Vector3(0, 0, 0);

    public PlayerComponent(float mass) {
        this.mass = mass;
    }
}
