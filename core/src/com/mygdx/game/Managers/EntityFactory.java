package com.mygdx.game.Managers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.EntityBuilder;


/**
 * Created by neiderm on 12/21/2017.
 */

public class EntityFactory {

    private EntityFactory() {
    }

    /*
     */
    public /* abstract */ static class GameObject {

        protected Model model;
        protected Vector3 size;
        protected String rootNodeId = null;
        protected btCollisionShape shape = null;

        private GameObject() {
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

            Entity e = create();

            // really? this will be bullet comp motion state linked to same copy of instance transform?
            // defensive copy, must NOT assume caller made a new instance!
            Matrix4 transform = new Matrix4().idt().trn(translation);

            e.add(new ModelComponent(model, transform, size, rootNodeId));
            e.add(new BulletComponent(shape, transform, mass));

            return e;
        }
    }

    /*
     * extended objects ... these would probably be implemented  by the "consumer" to meet specific needs
     */
    public static class SphereObject extends GameObject {

//        private float radius;

        public SphereObject(Model model, float radius) {

            super(model, new Vector3(radius, radius, radius));
//            this.radius = radius;
            this.shape = new btSphereShape(radius * 0.5f);
        }

/*        @Override
        public Entity create(float mass, Vector3 translation) {

            return super.create(mass, translation, new btSphereShape(radius * 0.5f));
        }*/
    }

    public static class BoxObject extends GameObject {

        public BoxObject(Vector3 size, Model model) {
            super(model, size);
            this.shape = new btBoxShape(size.cpy().scl(0.5f));
        }

/*        public BoxObject(Vector3 size, Model model, final String rootNodeId) {
            super(size, model, rootNodeId);
            this.shape = new btBoxShape(size.cpy().scl(0.5f));
        }

        @Override
        public Entity create(float mass, Vector3 translation) {

            return super.create(mass, translation, new btBoxShape(size.cpy().scl(0.5f)));
        }*/
    }

    public static class StaticObject extends GameObject {

        public StaticObject(Model model, String rootNodeId) {
            super(model, rootNodeId, new Vector3(1, 1, 1));
        }

        @Override
        public Entity create() {
            return EntityBuilder.loadStaticEntity(this.model, this.rootNodeId);
        }
    }

    public static class KinematicObject extends GameObject {

        public KinematicObject(Model model) {
            this.model = model;
        }

        public KinematicObject(Model model, Vector3 size){
            super(model, size);
            this.shape = new btBoxShape(size.cpy().scl(0.5f));
        }

        public KinematicObject(Model model, String rootNodeId, Vector3 size) {
            super(model, rootNodeId, size);
            this.shape = new btBoxShape(size.cpy().scl(0.5f));
        }

        public KinematicObject(Model model, float radius){
            super(model, new Vector3(radius, radius, radius));
            this.shape = new btSphereShape(radius * 0.5f);
        }

        @Override
        public Entity create(Vector3 translation) {
            return EntityBuilder.loadKinematicEntity(
                    this.model, this.rootNodeId, this.shape, translation, this.size);
        }

        @Override
        public Entity create(float mass, Vector3 translation, btCollisionShape shape) {

            return EntityBuilder.loadKinematicEntity(
                    this.model, this.rootNodeId, shape, translation, this.size);
        }
    }
}
