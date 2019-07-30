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
    private Vector3 tgtPosition = new Vector3();

    public Vector3 omniRadius = new Vector3();
    public Matrix4 tgtTransform;

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

    /*
     * sets the given T0 vector as origin location (e.g. if object location loaded from model
     */
    public OmniSensor(Entity target, Vector3 vT0) {

        this(target);
        this.vT0.set(vT0); // sensor origin
    }

    @Override
    public void setTarget(Entity target, Vector3 radius, boolean inverted){

        this.target = target;
        tgtTransform = target.getComponent(ModelComponent.class).modelInst.transform;
//        setTarget(target);

        this.inverted = inverted;
        this.omniRadius.set(radius);
    }

//    @Override
//    public void setTarget(Entity target){
//
//        this.target = target;
//        tgtTransform = target.getComponent(ModelComponent.class).modelInst.transform;
//
//        this.omniRadius.set(vS);
//    }

    @Override
    public void update(Entity sensor) {
        //                super.update(e);

        // grab the starting Origin (translation) of the entity from the instance data
        sensorOrigin.set(vT0);  // grab the starting Origin (translation) of the entity from the instance data

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
          // whatever ... target.getComponent(StatusComponent.class).lifeClock = 0;
        }
    }
}
