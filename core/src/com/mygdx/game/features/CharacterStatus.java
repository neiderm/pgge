package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.util.ModelInstanceEx;

/*
 * what is this?
 * Any object/entity that has a sort of life/health status
 */

public class CharacterStatus implements FeatureIntrf {


    private Ray lookRay = new Ray();
    private Vector3 tmpV3 = new Vector3();
    private Quaternion rotation = new Quaternion();
    private Vector3 direction = new Vector3(0, 0, -1); // vehicle forward

    @Override
    public void update(Entity e) {

        Matrix4 transform = e.getComponent(ModelComponent.class).modelInst.transform;
        lookRay.set(transform.getTranslation(tmpV3),
                ModelInstanceEx.rotateRad(direction.set(0, 0, -1), transform.getRotation(rotation)));

/*
        gameEventSignal.dispatch(hitDetectEvent.set(EVT_HIT_DETECT, lookRay, 0)); // maybe pass transform and invoke lookRay there
        gameEventSignal.dispatch(seeObjectEvent.set(EVT_SEE_OBJECT, lookRay, 0)); // maybe pass transform and invoke lookRay there
*/
    }

    protected Ray getLookRay() {
        return lookRay;
    }
}
