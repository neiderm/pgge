package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.util.IStatusUpdater;

/**
 * Created by utf1247 on 7/5/2018.
 */

public class StatusComponent implements Component {

    public Vector3 position;
    public Vector3 origin; // the reference point for determining an object has exitted the level
    public boolean isActive;
    public IStatusUpdater statusUpdater;
}
