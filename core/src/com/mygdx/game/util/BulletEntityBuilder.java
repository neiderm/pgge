package com.mygdx.game.util;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.ModelComponent;

/**
 * Created by mango on 4/1/18.
 */

public class BulletEntityBuilder extends BaseEntityBuilder {


    /*
     *  For the case of a static model, the Bullet wrapper provides a convenient method to create a
     *  collision shape of it:
     *   https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Using-models
     *  But in some situations having issues (works only if single node in model, and it has no local translation - see code in Bullet.java)
     */
    public static Entity load(
            Model model, String nodeID, btCollisionShape shape, Vector3 translation, Vector3 size) {

        Entity entity = new Entity();
        ModelInstance instance = ModelInstanceEx.getModelInstance(model, nodeID);

//        if (null != size)
// https://stackoverflow.com/questions/21827302/scaling-a-modelinstance-in-libgdx-3d-and-bullet-engine
        // note : modelComponent creating bounding box
        instance.nodes.get(0).scale.set(size);
        instance.calculateTransforms();

        // leave translation null if using translation from the model layout
//        if (null != translation)
        instance.transform.trn(translation);

        entity.add(new ModelComponent(instance));

        // special sauce here for static entity
        BulletComponent bc = new BulletComponent(shape, instance.transform, 0);
        entity.add(bc);

// set these flags in bullet comp?
        bc.body.setCollisionFlags(
                bc.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
        bc.body.setActivationState(Collision.DISABLE_DEACTIVATION);

        return entity;
    }
}
