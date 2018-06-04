package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.Components.PickRayComponent;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;


public class PickRaySystem extends EntitySystem implements EntityListener {

    private ImmutableArray<Entity> entities;


    private Matrix4 transformHACK;

    public void setTransformHACK(Matrix4 transformHACK){

        this.transformHACK = transformHACK;
    }


    private Quaternion rotation = new Quaternion();
    private static Vector3 position = new Vector3();
    private static Vector3 tmpV = new Vector3();
    private static Vector3 direction = new Vector3(0, 0, -1); // vehicle forward
    private Ray ray = new Ray();

    @Override
    public void update(float deltaTime) {

        ModelInstanceEx.rotateRad(direction.set(0, 0, -1), transformHACK.getRotation(rotation));

        transformHACK.getTranslation(position);

        Entity picked = applyPickRay(ray.set(position, direction));

        if (null != picked) {
            Matrix4 tmpM = picked.getComponent(ModelComponent.class).modelInst.transform;
            tmpM.getTranslation(tmpV);
// have to getTranslation again, dumb
            RenderSystem.otherThings.add(GfxUtil.lineTo(transformHACK.getTranslation(position),
                    tmpV, Color.LIME));
        }
    }


    /*
     * Using raycast/vector-projection to detect object. Can be generalized as a sort of
     * collision detection. Would like to have the collision shape and/or raycast implemmetantion
     * as well as use of bullet collision detection applied to different classes of game objects
     * for different effects/systems (e.g. jewel collisions might be ray-cast, but projectiles
     * might merit shape accuracy.
     *  https://xoppa.github.io/blog/interacting-with-3d-objects/
     */
    public Entity applyPickRay(Ray ray) {

        Entity picked = null;
        float distance = -1f;

        for (Entity e : entities) {

            ModelComponent mc = e.getComponent(ModelComponent.class);

            mc.modelInst.transform.getTranslation(position).add(mc.center);

            if (false) {
                float dist2 = ray.origin.dst2(position);

                if (distance >= 0f && dist2 > distance)
                    continue;

                if (Intersector.intersectRaySphere(ray, position, mc.boundingRadius, null)) {
                    picked = e;
                    distance = dist2;
                }
            } else {
                final float len = ray.direction.dot(
                        position.x - ray.origin.x,
                        position.y - ray.origin.y,
                        position.z - ray.origin.z);

                if (len < 0f)
                    continue;

                float dist2 = position.dst2(
                        ray.origin.x + ray.direction.x * len,
                        ray.origin.y + ray.direction.y * len,
                        ray.origin.z + ray.direction.z * len);

                if (distance >= 0f && dist2 > distance)
                    continue;

                if (dist2 <= mc.boundingRadius * mc.boundingRadius) {
                    picked = e;
                    distance = dist2;
                }
            } // if ....
            /*            Gdx.app.log("asdf", String.format("mc.id=%d, dx = %f, pos=(%f,%f,%f)",
                    mc.id, distance, position.x, position.y, position.z ));*/
        }
        return picked;
    }

    @Override
    public void entityAdded (Entity entity){

//        addPickObject(entity) ;
    }

    @Override
    public void entityRemoved (Entity entity){
    // empty
    }


    @Override
    public void addedToEngine(Engine engine) {

//        this.engine = engine;

        // Grabs all entities with desired components
        entities = engine.getEntitiesFor(Family.all(PickRayComponent.class).get());

        // listener for these so that their bullet objects can be dispose'd
        engine.addEntityListener(Family.all(PickRayComponent.class).get(), this);
    }

    @Override
    public void removedFromEngine(Engine engine) {

        engine.removeEntityListener(this); // Ashley bug (doesn't remove listener when system removed?

        // tmp ... loop all Bullet entities to destroy resources
/*        for (Entity e : entities) {

            PickRayComponent bc = e.getComponent(PickRayComponent.class);

            if (null != bc) {
            }
        }*/
    }
}
