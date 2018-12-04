package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.util.IStatusUpdater;

/**
 * Created by utf1247 on 7/5/2018.
 */

public class StatusComponent implements Component {

    public IStatusUpdater statusUpdater;
    public Matrix4 transform;
    public Vector3 origin = new Vector3(0, 0, 0); // the reference point for determining an object has exitted the level
    public float boundsDst2; // the reference point for determining an object has exitted the level

    public StatusComponent() {

        Vector3 bounds = new Vector3(50, 50, 50);
        boundsDst2 = bounds.dst2(origin);
    }
}
