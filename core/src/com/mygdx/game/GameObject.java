package com.mygdx.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.Bullet;
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
    public String rootNodeId;
    public btCollisionShape shape;

    public GameObject() {
    }

    private GameObject(Model model, Vector3 size) {
        this.model = model;
        this.size = size;
    }

    public GameObject(Model model, Vector3 size, btCollisionShape shape) {
        this(model, size);
        this.shape = shape;
    }



    public Entity create(float mass, Vector3 translation) {
        return create(mass, translation, this.shape);
    }

    private Entity create(float mass, Vector3 translation, btCollisionShape shape) {
        return load(this.model, this.rootNodeId, this.size, mass, translation, shape);
    }


    public static Entity load(Model model, String rootNodeId){
        //return load(model, rootNodeId, null);
        // we can set trans default value as do-nothing 0,0,0 so long as .trn() is used (adds offset onto present trans value)
//        return load(model, rootNodeId, new Vector3(0, 0, 0));
// return load(model, rootNodeId, null, new Vector3(0, 0, 0)); // where ..............
        return load(model, rootNodeId, new Vector3(0, 0, 0)); // where ..............
    }

/*    private static Entity load(Model model, String rootNodeId, Vector3 size){
//        return load(model, rootNodeId, scale, null);
// note: to do-no-harm here, the translation of 0,0,0 would need to be an offset (as opposed to absolute)
        return load(model, rootNodeId, size, new Vector3(0, 0, 0));
    }*/

    private static Entity load(Model model, String rootNodeId, Vector3 translation){

        return load(model, rootNodeId, new Vector3(1, 1, 1), translation);
    }

    public static Entity load(Model model, String rootNodeId, Vector3 size, Vector3 translation)
    {
        Entity e = new Entity();

        if (null != rootNodeId) {
            ModelInstance instance = EntityBuilder.getModelInstance(model, rootNodeId);
            e.add(new ModelComponent(instance, size));
        } else {
            e.add(new ModelComponent(model, size));
        }

        // leave translation null if using translation from the model layout 
        if (null != translation) {
            ModelInstance instance = e.getComponent(ModelComponent.class).modelInst;
            instance.transform.trn(translation);
        }
        else
            translation = null; // GN: tmp  // throw new GdxRuntimeException("?");

        return e;
    }

    public static Entity load(
            Model model, String nodeID, float mass, Vector3 translation, btCollisionShape shape) {
        return load(model, nodeID, null, mass, translation, shape);
    }

    public static Entity load(
            Model model, String nodeID, Vector3 size, float mass, Vector3 translation, btCollisionShape shape) {

        Entity e = load(model, nodeID, size, translation);
        ModelInstance instance = e.getComponent(ModelComponent.class).modelInst;

        if (null != nodeID) {
            if (null == shape) { // "Platform001"
                shape = EntityBuilder.createConvexHullShape(instance.getNode(nodeID).parts.get(0).meshPart);
            }
        }else{
            nodeID = null; // does it?
        }

        e.add(new BulletComponent(shape, instance.transform, mass));

        return e;
    }

    /*
       work around for "gaps" around convex hull cube shapes created from mesh :(
    */
    public static Entity load(Model model, String nodeID, float mass) {
        return load(model, nodeID, null, mass, null);
    }

    public static Entity load(Model model) {
        btBvhTriangleMeshShape shape = null;
        //shape = new btBvhTriangleMeshShape(model.meshParts);
        return load(model, null, shape, new Vector3(0, 0, 0), null);
    }

    private static Entity load(
            Model model, String nodeID, Vector3 size, float mass, Vector3 translation) {

        Entity e = load(model, nodeID, size, translation);
        ModelInstance instance = e.getComponent(ModelComponent.class).modelInst;

        BoundingBox boundingBox = new BoundingBox();
//        Vector3 center = new Vector3();
        Vector3 dimensions = new Vector3();
        instance.calculateBoundingBox(boundingBox);
//        boundingBox.getCenter(center);
//        boundingBox.getDimensions(dimensions);
        e.add(new BulletComponent(
                new btBoxShape(boundingBox.getDimensions(dimensions).scl(0.5f)), instance.transform, mass));

        return e;
    }

    /*
     *  https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Using-models
     *  For the case of a static model, the Bullet wrapper provides a convenient method to create a collision shape of it:
     *  But it's not exactly the right thing here
     */
    public static Entity load(
            Model model, String nodeID, btCollisionShape shape, Vector3 translation, Vector3 size){

        if (null != shape) {
            // have a shape so use it
        } else if (null != size) {
            // Convex Hull
        } else if (null == size && null == shape) {
//            shape = new btBvhTriangleMeshShape(model.meshParts);
            // obtainStaticNodeShape works for terrain mesh - selects a triangleMeshShape  - but is overkill.
            // In other situations causes issues (works only if single node in model, and it has no local translation - see code in Bullet.java)
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
