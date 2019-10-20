package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.sceneLoader.GameFeature;

/**
 * Created by neiderm on 7/5/2019.
 * <p>
 * This can be a "generic" handler for a sensor. assigned a single target Entity to be sensing for.
 */

public class OmniSensor extends SensorAdaptor {

    private Vector3 sensorOrigin = new Vector3(); // the reference point for determining an object has exitted the level
    private Vector3 bounds = new Vector3();
    private Vector3 tgtPosition = new Vector3();

    Vector3 omniRadius = new Vector3();

    private final Vector3 DEFAULT_RADIUS = new Vector3(1.5f, 1.5f, 1.5f); //


    public OmniSensor() {/* no-arg constructor */

        omniRadius.set(DEFAULT_RADIUS); // maybe .. idfk
    }

    // Pass in the tgt xform .. not the Entity?

//    private OmniSensor(Entity target) {
//
//        setTarget(
////                target,
//                DEFAULT_RADIUS);
//    }

    /*
     * sets the given T0 vector as origin location (e.g. if object location loaded from model
     */
//    protected OmniSensor(Entity target, Vector3 origin) {
//
//        this(target);
//        this.vT0.set(origin); // sensor origin
//    }

    @Override
    public void init(Object target) {

//        setTarget(
////                (Entity) target,
//                vS /* radius */, vT /* origin */);

        this.omniRadius.set(vS);
        this.sensorOrigin.set(vT);
    }

//    private void setTarget(
////            Entity target,
//            Vector3 radius) {
//
////        this.target = target;
//        this.omniRadius.set(radius);
//    }

//    private void setTarget(
////            Entity target,
//            Vector3 radius, Vector3 origin) {
//
//        this.omniRadius.set(radius);
//        this.sensorOrigin.set(origin);
//    }

    private Vector3 vvv = new Vector3();

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        // grab the starting Origin (translation) of the entity from the instance data
// hmmmm ...  it could have been spawned "up there" and now falling to the floor ... so vT0 as handed by GameObject constructor is not the origin we're looking for!
////        sensorOrigin.set(vT0);  // grab the starting Origin (translation) of the entity from the instance data

        ModelComponent mc = sensor.getComponent(ModelComponent.class);
        Matrix4 transform = mc.modelInst.transform;
        vvv = transform.getTranslation(vvv);
        sensorOrigin.set(vvv);


        bounds.set(sensorOrigin);
        bounds.add(omniRadius);

        float boundsDst2 = bounds.dst2(sensorOrigin);

        if (null == target) {

            GameFeature playerFeature = GameWorld.getInstance().getFeature("Player");
            if (null != playerFeature) {

                //// ha ha hackit hgacktity
                target = playerFeature.getEntity();
            }
        } else {
            Matrix4 tgtTransform = target.getComponent(ModelComponent.class).modelInst.transform;

            if (null != tgtTransform)
                tgtPosition = tgtTransform.getTranslation(tgtPosition);


            isTriggered = false; // hmmm should be "non-latching? "

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
}
