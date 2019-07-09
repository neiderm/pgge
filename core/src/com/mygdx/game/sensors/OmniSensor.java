package com.mygdx.game.sensors;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.components.ModelComponent;

/**
 * Created by neiderm on 7/5/2018.
 * <p>
 * This can be a "generic" handler for a sensor. assigned a single target Entity to be sensing for.
 */

public class OmniSensor extends VectorSensor {

    private Vector3 sensorOrigin = new Vector3(); // the reference point for determining an object has exitted the level
    private Vector3 bounds = new Vector3();
    private Vector3 omniRadius = new Vector3(1.5f, 1.5f, 1.5f);
    private Vector3 tgtPosition = new Vector3();
    private Matrix4 tgtTransform;
    private boolean inverted = false;

    protected OmniSensor(Entity target, Vector3 omniRadius, boolean inverted) {
        this(target);
        this.inverted = true;
        this.omniRadius.set(omniRadius);
    }

    protected OmniSensor(Entity target) {

        this.target = target;
        tgtTransform = target.getComponent(ModelComponent.class).modelInst.transform;
    }

    @Override
    public void update(Entity sensor) {
        //                super.update(e);

        sensorOrigin = sensor.getComponent(ModelComponent.class).modelInst.transform.getTranslation(sensorOrigin);
        bounds.set(sensorOrigin);
        bounds.add(omniRadius);
        float boundsDst2 = bounds.dst2(sensorOrigin);
        tgtPosition = tgtTransform.getTranslation(tgtPosition);

        if (inverted) {
            if (tgtPosition.dst2(sensorOrigin) > boundsDst2) {
                isTriggered = true;
            }
        } else {
            if (tgtPosition.dst2(sensorOrigin) < boundsDst2) {
                isTriggered = true;
            }
        }
    }
}
