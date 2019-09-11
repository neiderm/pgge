package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;

/**
 * Created by neiderm on 7/5/2019.
 *
 * base type for a "Feature with a Target"
 */
class SensorAdaptor extends FeatureAdaptor {

    Entity target;
    boolean inverted;
    boolean isTriggered;

    @Override
    public void init(Object target){

        this.target = (Entity)target;
    }
}
