package com.mygdx.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
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
private final static boolean asdf = false;

    /*
     * Load a static^H^H^H^H^H^H non-bullet entity from object (will be static or moved about by
     * other impetus (e.g. cam chaser)
     * TODO: add transform argument to loadStaticEntity
     */
    public Entity create(Vector3 translation) {

        Entity e = loadStaticEntity(this.model, this.rootNodeId, this.size, null);
if (asdf) {
    Matrix4 transform = e.getComponent(ModelComponent.class).modelInst.transform;
    transform.idt().trn(translation);
}
        return e;
    }

    public Entity create(float mass, Vector3 translation, btCollisionShape shape) {

        return (loadDynamicEntity(model, this.rootNodeId, size, mass, translation, shape));
    }

    public static Entity loadStaticEntity(Model model, String rootNodeId, Vector3 scale, Vector3 translation)
    {
        Entity e = new Entity();

        if (null != rootNodeId) {
            ModelInstance instance = EntityBuilder.getModelInstance(model, rootNodeId);
            e.add(new ModelComponent(instance, scale));
        } else {
            e.add(new ModelComponent(model, new Matrix4(), scale, null));
        }

        return e;
    }

    public static Entity loadDynamicEntity(
            Model model, String nodeID, Vector3 size, float mass, Vector3 translation, btCollisionShape shape) {

        Entity e = loadStaticEntity(model, nodeID, size, null);
        ModelInstance instance = e.getComponent(ModelComponent.class).modelInst;

//        if (null != size){
//            instance.transform.scl(size); // if mesh must be scaled, do it before creating the hull shape
//        }

        if (null == shape) {
            if (null != nodeID) {
                shape = EntityBuilder.createConvexHullShape(instance.getNode(nodeID).parts.get(0).meshPart);
            }else{
                final Mesh mesh = model.meshes.get(0);
                shape = new btConvexHullShape(mesh.getVerticesBuffer(), mesh.getNumVertices(), mesh.getVertexSize());
//                shape = createConvexHullShape(mesh.getVerticesBuffer(), mesh.getNumVertices(), mesh.getVertexSize(), true);
            }
        }

        e.add(new BulletComponent(shape, instance.transform, mass));

        // set to translation here if you don't want what the primitivesModel gives you
if (!asdf) {
    if (null != translation) {
        instance.transform.trn(translation);
        e.getComponent(BulletComponent.class).body.setWorldTransform(instance.transform);
    }
}

        if (null != size){
            instance.transform.scl(size); // if mesh must be scaled, do it before^H^H^H^H^H^H  ?????
        }

        return e;
    }

    public static Entity loadKinematicEntity(
            Model model, String nodeID, btCollisionShape shape, Vector3 trans, Vector3 size){

        Entity entity = loadDynamicEntity(model, nodeID, size, 0, trans, shape);

        // special sauce here for static entity
        // called loadDynamicEntity w/ mass==0, so it's BC will NOT have a motionState (which is what we
        // want for this object) so we do need to update the bc.body with the location vector we got from the model
        BulletComponent bc = entity.getComponent(BulletComponent.class);

        if (null == trans)
        {
            Vector3 tmp = new Vector3();
            entity.getComponent(ModelComponent.class).modelInst.transform.getTranslation(tmp);
            bc.body.translate(tmp); // if translation param not given, need to sync body /w mesh instance
        }

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

        return loadKinematicEntity(
                model, null, new btBvhTriangleMeshShape(model.meshParts), new Vector3(0, 0, 0), null);
    }
}
