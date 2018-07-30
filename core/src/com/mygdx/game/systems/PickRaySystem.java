package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.PickRayComponent;
import com.mygdx.game.util.EventQueue;
import com.mygdx.game.util.GameEvent;


public class PickRaySystem extends IteratingSystem {

    private EventQueue eventQueue;
    private static Vector3 position = new Vector3();


    public PickRaySystem(Signal<GameEvent> gameEventSignal){

        super(Family.all(PickRayComponent.class).get());

        eventQueue = new EventQueue();
        gameEventSignal.add(eventQueue);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        // empty
    }

    @Override
    public void update(float deltaTime) {

// first we have to find out who's listening for notificaitons
        for (GameEvent event : eventQueue.getEvents()) {

            switch (event.type) {
                case RAY_PICK:
                    handleEvent(event);
                    break;
                case RAY_DETECT:
                    handleEvent(event);
                    break;
                default:
                    break;
            }
        }
   }

    private void handleEvent(GameEvent event) {

        Entity picked = applyPickRay((Ray) event.object);

//        if (null != picked)
        {
            event.callback(picked, event.type);
        }
    }


//    Vector3 interSection = new Vector3();
    /*
     * Using raycast/vector-projection to detect object. Can be generalized as a sort of
     * collision detection. Would like to have the collision shape and/or raycast implemmetantion
     * as well as use of bullet collision detection applied to different classes of game objects
     * for different effects/systems (e.g. jewel collisions might be ray-cast, but projectiles
     * might merit shape accuracy.
     *  https://xoppa.github.io/blog/interacting-with-3d-objects/
     */
    private Entity applyPickRay(Ray ray) {

        Entity picked = null;
        float distance = -1f;

        for (Entity e : getEntities()) {

            ModelComponent mc = e.getComponent(ModelComponent.class);
            mc.modelInst.transform.getTranslation(position).add(mc.center);

            // gets the distance of the center of object to the ray, for more accuracy
            float dist2 = intersect(ray, position, mc.boundingRadius, null);
            if (dist2>=0)  dist2 = ray.origin.dst2(position);//ray.origin to object distance works ok

            if ((dist2 < distance || distance < 0f) && dist2 >= 0) {
                picked = e;
                distance = dist2;
            }
        }
        return picked;
    }

    /*
     * Intersector.intersectRaySphere() modified to return the "distance" of ray endpoint
     *  to object center ( >= 0 )  or -1 if no intersection
     *  Create an extended Ray class for this.
     */
    private float intersect(Ray ray, Vector3 center, float radius, Vector3 intersection) {

        float dst2 = -1;

        final float len = ray.direction.dot(
                center.x - ray.origin.x,
                center.y - ray.origin.y,
                center.z - ray.origin.z);

        if (len >= 0f) { // if negative then the center is behind the ray

            dst2 = center.dst2(
                    ray.origin.x + ray.direction.x * len,
                    ray.origin.y + ray.direction.y * len,
                    ray.origin.z + ray.direction.z * len);

            final float r2 = radius * radius;
            if (dst2 > r2) return -1; // no intersection
/*
        if (intersection != null) intersection.set(ray.direction).scl(len - (float)Math.sqrt(r2 - dst2)).add(ray.origin);
*/
        }
        return dst2;
    }

}
