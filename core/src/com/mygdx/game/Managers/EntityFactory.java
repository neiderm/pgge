package com.mygdx.game.Managers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
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
        protected btCollisionShape shape = null;

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

            Entity e = create();

            // really? this will be bullet comp motion state linked to same copy of instance transform?
            // defensive copy, must NOT assume caller made a new instance!
            Matrix4 transform = new Matrix4().idt().trn(translation);

            e.add(new ModelComponent(model, transform, size, rootNodeId));
            e.add(new BulletComponent(shape, transform, mass));

            return e;
        }
    }


}
