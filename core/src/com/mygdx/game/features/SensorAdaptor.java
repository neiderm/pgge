package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;

/**
 * Created by neiderm on 7/5/2018.
 */

public class SensorAdaptor extends FeatureAdaptor {

    protected boolean isTriggered;

    public boolean getIsTriggered() {
        return isTriggered;
    }

    @Override
    public void update(Entity e) {/*mt*/}
}
