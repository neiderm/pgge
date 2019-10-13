package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.components.ModelComponent;

/**
 * Created by neiderm on 7/5/2019.
 *
 * base type for a "Feature with a Target" ... the sensor is distinct from the generic feature
 * in its targetting characterisitcs.
 */
class SensorAdaptor extends FeatureAdaptor {

    Entity target;
    boolean inverted;
    boolean isTriggered;

    @Override
    public void init(Object target){

        this.target = (Entity)target;
    }

    /*
     * position sensor at offset from player (resulting position vector passed to base handler in vT0)
     */
    @Override
    public void onActivate(Entity ee) {

        // base OFFSETS  xlation to vT ... so here initialize vT so the offset is against the target location then call the base nethiod

        Vector3 translation = new Vector3();

        ModelComponent mc = target.getComponent(ModelComponent.class);
        translation = mc.modelInst.transform.getTranslation(translation);

        /* position vector offsets by user value from json, to be loaded by super method */
        vT0.set(vT).add(translation);

        super.onActivate(ee);
    }
}
