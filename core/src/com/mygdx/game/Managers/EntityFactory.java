package com.mygdx.game.Managers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.ModelComponent;


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

        private GameObject() {
        }

        public GameObject(Vector3 size, Model model) {
            this.model = model;
            this.size = size;
        }

        public GameObject(Vector3 size, Model model, String rootNodeId) {
            this.model = model;
            this.size = size;
            this.rootNodeId = rootNodeId;
        }

        protected Entity create() {
            return new Entity();
        }

        ///*
        public Entity create(float mass, Vector3 translation) {
            return new Entity();
        }

        //*/
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

    public static class SphereObject extends GameObject {

        private float radius;

        public SphereObject(float radius, Model model) {

            super(new Vector3(radius, radius, radius), model);
            this.radius = radius;
        }

        @Override
        public Entity create(/* Model model, */ float mass, Vector3 translation) {

            return super.create(mass, translation, new btSphereShape(radius * 0.5f));
        }
    }

    public static class BoxObject extends GameObject {

        public BoxObject(Vector3 size, Model model) {
            super(size, model);
        }

        public BoxObject(Vector3 size, Model model, final String rootNodeId) {
            super(size, model, rootNodeId);
        }

        @Override
        public Entity create(/* Model model, */ float mass, Vector3 translation) {

            return super.create(mass, translation, new btBoxShape(size.cpy().scl(0.5f)));
        }
    }

    /*
     * we might want lots of these ... islands in the sky, all made of mesh shapes
     */
    public static class LandscapeObject extends GameObject {

        //         Entity create(final Array<T> meshParts, Matrix4 transform){ ??????
        public Entity create(Model model, Matrix4 transform) {

            Entity e = create();

            e.add(new BulletComponent(
                    new btBvhTriangleMeshShape(model.meshParts), transform));

            e.add(new ModelComponent(model, transform));

            return e;
        }
    }


    /*
     derived factories do special sauce for static vs dynamic entities:
     */
    private abstract static class EntiteeFactory<T extends GameObject> {

//        T object;

        private EntiteeFactory() {
        }
/*
        EntiteeFactory(T object){
            this.object = object;
        }
*/
/*
        Entity create() {
            return create(0, new Vector3(0, 0, 0));
        }
*/
/*
        private Entity create(T object, float mass, Vector3 translation) {
            return object.create(mass, translation);
        }
*/
    }

    public static class StaticEntiteeFactory<T extends GameObject> extends EntiteeFactory {
        /*
                StaticEntiteeFactory(T object){
                    super(object);
                }
        */
        public Entity create(T object, Vector3 translation) {

            Entity e = object.create(0f, translation);

            // special sauce here for static entity
            Vector3 tmp = new Vector3();
            BulletComponent bc = e.getComponent(BulletComponent.class);
            ModelComponent mc = e.getComponent(ModelComponent.class);

            // bc.body.translate(tmp.set(modelInst.transform.val[12], modelInst.transform.val[13], modelInst.transform.val[14]));
            bc.body.translate(mc.modelInst.transform.getTranslation(tmp));

            /* need ground & landscape objects to be kinematic: once the player ball stopped and was
            deactivated by the bullet dynamics, it would no longer move around under the applied force.
            ONe small complication is the renderer has to know which are the active dynamic objects
            that it has to "refresh" the scaling in the transform (because goofy bullet messes with
            the scaling!). So here we set a flag to tell renderer that it doesn't have to re-scale
            the kinematic object (need to do a "kinematic" component to deal w/ this).
             */
            bc.body.setCollisionFlags(
                    bc.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);

            bc.body.setActivationState(Collision.DISABLE_DEACTIVATION);

            bc.sFlag = true;

            // static entity not use motion state so just set the scale on it once and for all
            mc.modelInst.transform.scl(mc.scale);

            return e;
        }
    }

}
