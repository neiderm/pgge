package com.mygdx.game.features;

/**
 * Created by neiderm on 8/24/2019.
 * <p>
 * Out-of-bounds sensor is simply an inverted kill sensor
 */

public class OOBSensor extends KillSensor {

    @Override

    public void init(Object obj){

        super.init(obj);

        inverted = true;
    }
}
