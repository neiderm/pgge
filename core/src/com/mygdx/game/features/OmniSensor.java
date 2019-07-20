package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;

/**
 * Created by neiderm on 7/5/2018.
 * <p>
 * This can be a "generic" handler for a sensor. assigned a single target Entity to be sensing for.
 */

public class OmniSensor extends SensorAdaptor {

    private Vector3 sensorOrigin = new Vector3(); // the reference point for determining an object has exitted the level
    private Vector3 bounds = new Vector3();
    private Vector3 omniRadius = new Vector3();
    private Vector3 tgtPosition = new Vector3();
    private Matrix4 tgtTransform;

    private final Vector3 DEFAULT_RADIUS = new Vector3(1.5f, 1.5f, 1.5f); //

    public OmniSensor (){/*mt*/
    }

    /* Pass in the tgt xform .. not the Entity!
      * getComponent(ModelComponent.class).modelInst.transform
     */
    public OmniSensor(Entity target, Vector3 omniRadius, boolean inverted) {

        setTarget(target, omniRadius, inverted);
    }

    protected OmniSensor(Entity target) {

        setTarget(target, DEFAULT_RADIUS, false);
    }

    @Override
    public void setTarget(Entity target, Vector3 radius, boolean inverted){

        this.target = target;
        this.inverted = inverted;
        this.omniRadius.set(radius);

        tgtTransform = target.getComponent(ModelComponent.class).modelInst.transform;
    }

    @Override
    public void update(Entity sensor) {
        //                super.update(e);

        // grab the starting Origin (translation) of the entity from the instance data
        sensorOrigin.set(vT0);  // grab the starting Origin (translation) of the entity from the instance data
// .... not working because the vr_zone exit sensor is tied to model geometry so it's in there, just not in instancd data ;)

//        sensorOrigin = sensor.getComponent(ModelComponent.class).modelInst.transform.getTranslation(sensorOrigin);

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

        if (getIsTriggered()) {
            target.getComponent(StatusComponent.class).lifeClock = 0;
        }
    }
}
