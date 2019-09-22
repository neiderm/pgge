package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.util.GfxUtil;

/**
 * Created by neiderm on 7/5/2019.
 * <p>
 * sensor for close proximity in one direction to a target
 */

public class VectorSensor extends SensorAdaptor {

    private Vector3 trans = new Vector3();
    private GfxUtil lineInstance = new GfxUtil();

    private Vector3 myPos = new Vector3();
    private Vector3 targetPos = new Vector3();
    private Ray lookRay = new Ray();
    private Vector3 direction = new Vector3(0, 0, -1); // vehicle forward
    private Quaternion rotation = new Quaternion();

    public VectorSensor() {/*mt*/}

    public VectorSensor(Entity target) {
        this.target = target;
    }

    @Override
    public void update(Entity sensor) {

        //        super.update(me);

        float senseZoneRadius = 2.0f;
        float senseZoneDistance = 5.0f;

        Matrix4 plyrTransform = target.getComponent(ModelComponent.class).modelInst.transform;
        targetPos = plyrTransform.getTranslation(targetPos);

        Matrix4 sensTransform = sensor.getComponent(ModelComponent.class).modelInst.transform;
        myPos = sensTransform.getTranslation(myPos);

        lookRay.set(sensTransform.getTranslation(myPos), // myPos
                GfxUtil.rotateRad(direction.set(0, 0, -1), sensTransform.getRotation(rotation)));

        /* add scaled look-ray-unit-vector to sensor position */
        myPos.add(lookRay.direction.scl( senseZoneDistance )); // we'll see
///*
        RenderSystem.debugGraphics.add(lineInstance.lineTo(
                sensTransform.getTranslation(trans),
                myPos,
                Color.SALMON));
//*/
        // take differnece from  center-of-sense-zone-sphere to target.
        // If that distance fall within the sense radius.
        myPos.sub(targetPos);

        if (Math.abs(myPos.x) < senseZoneRadius && Math.abs(myPos.z) < senseZoneRadius) {
            isTriggered = true;
        }
    }
}
