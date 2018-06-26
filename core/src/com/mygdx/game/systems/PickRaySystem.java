package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.Components.PickRayComponent;
import com.mygdx.game.util.EventQueue;
import com.mygdx.game.util.GameEvent;
import com.mygdx.game.util.ModelInstanceEx;


public class PickRaySystem extends IteratingSystem implements EntityListener {

    private EventQueue eventQueue;

    private Quaternion rotation = new Quaternion();
    private static Vector3 position = new Vector3();
    private static Vector3 direction = new Vector3(0, 0, -1); // vehicle forward
    private Ray ray = new Ray();


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

        // super?

        GameEvent activeEvent = null; // tmp: need a queue of listeners

// first we have to find out who's listening for notificaitons
        for (GameEvent event : eventQueue.getEvents()) {
            switch (event.type) {

                case RAY_PICK:
                    activeEvent = event; // tmp: need to update the queue of listeners for this event
                    break;
                case RAY_DETECT:
                    activeEvent = event; // tmp: need to update the queue of listeners for this event
                    break;
                default:
                    ;
            }
        }

        // no point in doing any more unless we have at least one listener!
        if (null != activeEvent) {
            Matrix4 tmpM = (Matrix4) activeEvent.object;
            Entity picked =
                    applyPickRay(ray.set(tmpM.getTranslation(position),
                            ModelInstanceEx.rotateRad(direction.set(0, 0, -1), tmpM.getRotation(rotation))
                    ));

            if (null != picked) {
                activeEvent.callback(picked, activeEvent.type);
            }
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
    public float intersect(Ray ray, Vector3 center, float radius, Vector3 intersection) {

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

    @Override
    public void entityAdded (Entity entity){
        // empty
    }

    @Override
    public void entityRemoved (Entity entity){
        // empty
    }

    @Override
    public void removedFromEngine(Engine engine) {

        super.removedFromEngine(engine);
        engine.removeEntityListener(this); // Ashley bug (doesn't remove listener when system removed?
    }
}
