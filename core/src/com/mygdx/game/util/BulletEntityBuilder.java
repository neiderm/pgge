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

    private btCollisionShape shape;

    BulletEntityBuilder() {
    }

/*    public BulletEntityBuilder(Model model, Vector3 size, btCollisionShape shape) {
        super(model, size);
        this.shape = shape;
    }*/


    public Entity create(float mass, Vector3 translation) {
        return load(this.model, this.rootNodeId, this.size, mass, translation, shape);
    }


    static Entity load(
            Model model, String nodeID, Vector3 size, float mass, Vector3 translation, btCollisionShape shape) {

        Entity e = load(model, nodeID, size, translation);
        ModelInstance instance = e.getComponent(ModelComponent.class).modelInst;

        if (null != nodeID) {
            if (null == shape) { // "Platform001"
                shape = MeshHelper.createConvexHullShape(instance.getNode(nodeID));
            }
        }

        e.add(new BulletComponent(shape, instance.transform, mass));

        return e;
    }

    /*
     *  For the case of a static model, the Bullet wrapper provides a convenient method to create a
     *  collision shape of it:
     *   https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Using-models
     *  But in some situations having issues (works only if single node in model, and it has no local translation - see code in Bullet.java)
     */
    public static Entity load(
            Model model, String nodeID, btCollisionShape shape, Vector3 translation, Vector3 size){

        if (null != shape) {
            // have a shape so use it
        } else if (null != size) {
            // Convex Hull
        } else /* if ( null == size &&   null == shape) */ {
//            shape = new btBvhTriangleMeshShape(model.meshParts);
            // obtainStaticNodeShape works for terrain mesh - selects a triangleMeshShape  - but is overkill.
            shape = Bullet.obtainStaticNodeShape(model.nodes);

            //TODO: check conditions and validity of obtain static shape, last resort, bounding box
            //entity = load(model, nodeID, size, 0, translation);
        }

        Entity entity = load(model, nodeID, size, 0, translation, shape);

        // special sauce here for static entity
        BulletComponent bc = entity.getComponent(BulletComponent.class);

// set these flags in bullet comp?
        bc.body.setCollisionFlags(
                bc.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
        bc.body.setActivationState(Collision.DISABLE_DEACTIVATION);

        return entity;
    }
}
