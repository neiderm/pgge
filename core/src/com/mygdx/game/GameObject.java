package com.mygdx.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
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

    public Entity create() {
        return new Entity();
    }

    /*
     * static^H^H^H^H^H^H non-bullet entity, will be static or moved about by other impetus (e.g. cam chaser)
     */
    public Entity create(Vector3 translation) {

        Entity e = create();

        Matrix4 transform = new Matrix4().idt().trn(translation);

        e.add(new ModelComponent(model, transform, size, rootNodeId));

        return e;
    }

    /*
     falling boxes and globes
     */
    public Entity create(float mass, Vector3 translation) {

        if (null == this.shape) {
            return new Entity(); // nfi
        }

        return create(mass, translation, this.shape);
    }

    public Entity create(float mass, Vector3 translation, btCollisionShape shape) {

        return (create(
                model, rootNodeId, size, mass, translation, shape));
    }

    public static Entity create(
            Model model, String rootNodeId, Vector3 size, float mass, Vector3 translation, btCollisionShape shape){

        Entity e = new Entity();

        // really? this will be bullet comp motion state linked to same copy of instance transform?
        // defensive copy, must NOT assume caller made a new instance!
        Matrix4 transform = new Matrix4().idt().trn(translation);

        e.add(new ModelComponent(model, transform, size, rootNodeId));
        e.add(new BulletComponent(shape, transform, mass));

        return e;
    }

    public static Entity loadStaticEntity(Model model, String rootNodeId)
    {
        return EntityBuilder.loadStaticEntity(model, rootNodeId);
    }

    public static Entity loadDynamicEntity(
            Model model, btCollisionShape shape, String nodeID, float mass, Vector3 translation, Vector3 size) {

        return EntityBuilder.loadDynamicEntity(model, shape, nodeID, mass, translation, size);
    }

    public static Entity loadTriangleMesh(Model model) {

        return EntityBuilder.loadKinematicEntity(
                model, null, new btBvhTriangleMeshShape(model.meshParts), new Vector3(0, 0, 0), null);
    }
}
