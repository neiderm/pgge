package com.mygdx.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.ModelComponent;

/**
 * Created by mango on 4/1/18.
 */

public class GameObject {

    public Model model;
    public Vector3 size;
    public String rootNodeId = null;
    public btCollisionShape shape = null;

    public GameObject() {
    }

    public GameObject(Model model, Vector3 size) {
        this.model = model;
        this.size = size;
    }

    public GameObject(Model model, String rootNodeId, Vector3 size) {
        this(model, size);
        this.rootNodeId = rootNodeId;
    }

    public Entity create(Vector3 translation) {
        return load(
                this.model, this.rootNodeId, this.shape, translation, this.size);
    }

    public Entity create(float mass, Vector3 translation) {

        return create(mass, translation, this.shape);
    }

    public Entity create(float mass, Vector3 translation, btCollisionShape shape) {

        return (load(this.model, this.rootNodeId, this.size, mass, translation, shape));
    }


    public static Entity load(Model model, String rootNodeId, Vector3 scale){
//        return load(model, rootNodeId, scale, null);
// note: to do-no-harm here, the translation of 0,0,0 would need to be an offset (as opposed to absolute)
        return load(model, rootNodeId, scale, new Vector3(0, 0, 0));
    }

    public static Entity load(Model model, String rootNodeId, Vector3 scale, Vector3 translation)
    {
        Entity e = new Entity();

        if (null != rootNodeId) {
            ModelInstance instance = EntityBuilder.getModelInstance(model, rootNodeId);
            e.add(new ModelComponent(instance, scale));
        } else {
            e.add(new ModelComponent(model, new Matrix4(), scale, null));
        }

        // leave translation null if using translation from the model layout 
        if (null != translation) {
            ModelInstance instance = e.getComponent(ModelComponent.class).modelInst;
            instance.transform.trn(translation);
        }
        else
            translation = null; // GN: tmp

        return e;
    }


    public static Entity load(
            Model model, String nodeID, Vector3 size, float mass, Vector3 translation, btCollisionShape shape) {

        Entity e = load(model, nodeID, size, translation);
        ModelInstance instance = e.getComponent(ModelComponent.class).modelInst;

//        if (null != size){
//            instance.transform.scl(size); // if mesh must be scaled, do it before creating the hull shape
//        }

        if (null == shape) { // "Platform001"
                shape = EntityBuilder.createConvexHullShape(instance.getNode(nodeID).parts.get(0).meshPart);
        }

        e.add(new BulletComponent(shape, instance.transform, mass));

        if (null != size){
            instance.transform.scl(size); // if mesh must be scaled, do it before^H^H^H^H^H^H  ?????
        }

        return e;
    }

    /*
       work around for "gaps" around convex hull cube shapes created from mesh :(
    */
    public static Entity load(
            Model model, String nodeID, float mass, Vector3 translation) {

        Entity e = load(model, nodeID, null, translation);
        ModelInstance instance = e.getComponent(ModelComponent.class).modelInst;

        BoundingBox boundingBox = new BoundingBox();
//        Vector3 center = new Vector3();
        Vector3 dimensions = new Vector3();
        instance.calculateBoundingBox(boundingBox);
//        boundingBox.getCenter(center);
        boundingBox.getDimensions(dimensions);

        e.add(new BulletComponent(new btBoxShape(dimensions.cpy().scl(0.5f)), instance.transform, mass));

        return e;
    }

    public static Entity load(
            Model model, String nodeID, btCollisionShape shape, Vector3 trans, Vector3 size){

        Entity entity;

        if (null != size || null != shape) {
			// if size specified e.g. (1, 1, 1 would do) then you could also leave shape null and force
			// the convex hull to be used.
            entity = load(model, nodeID, size, 0, trans, shape);
        } else {
            // if shape not given then defaults to simple bounding box shape
            entity = load(model, nodeID, 0, trans);
        }

        // special sauce here for static entity
        // called loadDynamicEntity w/ mass==0, so it's BC will NOT have a motionState (which is what we
        // want for this object) so we do need to update the bc.body with the location vector we got from the model
        BulletComponent bc = entity.getComponent(BulletComponent.class);

// set these flags in bullet comp?
        bc.body.setCollisionFlags(
                bc.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
        bc.body.setActivationState(Collision.DISABLE_DEACTIVATION);

            /* need ground & landscape objects to be kinematic: once the player ball stopped and was
            deactivated by the bullet dynamics, it would no longer move around under the applied force.
            ONe small complication is the renderer has to know which are the active dynamic objects
            that it has to "refresh" the scaling in the transform (because goofy bullet messes with
            the scaling!). So here we set a flag to tell renderer that it doesn't have to re-scale
            the kinematic object (need to do a "kinematic" component to deal w/ this).
             */
        bc.sFlag = true;

        return entity;
    }

    public static Entity loadTriangleMesh(Model model) {

        return load(
                model, null, new btBvhTriangleMeshShape(model.meshParts), new Vector3(0, 0, 0), null);
    }
}
