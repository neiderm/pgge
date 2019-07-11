package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.util.ModelInstanceEx;

/**
 * Created by neiderm on 7/5/2018.
 * <p>
 * sensor for close proximity in one direction to a target
 */

public class VectorSensor extends SensorAdaptor {

    private Vector3 myPos = new Vector3();
    private Vector3 playerPos = new Vector3();
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
        Matrix4 plyrTransform = target.getComponent(ModelComponent.class).modelInst.transform;
        playerPos = plyrTransform.getTranslation(playerPos);

        Matrix4 sensTransform = sensor.getComponent(ModelComponent.class).modelInst.transform;
        myPos = sensTransform.getTranslation(myPos);

        lookRay.set(sensTransform.getTranslation(myPos),
                ModelInstanceEx.rotateRad(direction.set(0, 0, -1), sensTransform.getRotation(rotation)));

        myPos.add(lookRay.direction);
        myPos.sub(playerPos);

        if (Math.abs(myPos.x) < 1.0f && Math.abs(myPos.z) < 1.0f) {
            isTriggered = true;
        }
    }
}
