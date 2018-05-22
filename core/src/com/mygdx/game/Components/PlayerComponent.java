package com.mygdx.game.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by mango on 1/23/18.
 */

public class PlayerComponent implements Component {

    public boolean died = false;
    public Vector2 inpVect = new Vector2(0, 0); // control input vector

/*
as the player, I always interact thru the controller. But the controller can be attached to
different game object

e.g.

public InputReceiver inputReceiver
 */

    public PlayerComponent() {
        // empty
    }
}
