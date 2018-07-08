package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.util.IStatusUpdater;

/**
 * Created by utf1247 on 7/5/2018.
 */

public class StatusComponent implements Component {

    public IStatusUpdater statusUpdater;

    public Vector3 position;
    public Vector3 origin = new Vector3(0, 0, 0); // the reference point for determining an object has exitted the level
    public float boundsDst2; // the reference point for determining an object has exitted the level
    public boolean isActive;

    public StatusComponent() {

        Vector3 bounds = new Vector3(20, 20, 20);
        boundsDst2 = bounds.dst2(origin);
        isActive = true;
    }
}
