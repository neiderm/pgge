/*
 * Copyright (c) 2021 Glenn Neidermeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    private static final Vector3 position = new Vector3();

    private final EventQueue eventQueue;

    public PickRaySystem(Signal<GameEvent> gameEventSignal) {

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
        // first we have to find out who's listening for notifications
        for (GameEvent event : eventQueue.getEvents()) {

            switch (event.getEventType()) {

                case EVT_HIT_DETECT:
                    handleEvent(event);
                    break;
                case EVT_SEE_OBJECT:
                    handleEvent(event);
                    break;
                default:
                    break;
            }
        }
    }

    private void handleEvent(GameEvent event) {
        /*
         * likely to let this be in the Character event handler
         */
        Entity picked = applyPickRay((Ray) event.getObject());

        // always respond even if picked is null
        event.handle(picked, event.getEventType());
        // notification to picked? ... no it may be done in the overridden event.handle()er tho .
    }

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

            if (null != mc) {

                mc.modelInst.transform.getTranslation(position).add(mc.center);

                // gets the distance of the center of object to the ray, for more accuracy
                float dist2 = intersect(ray, position, mc.boundingRadius);

                if (dist2 >= 0) {
                    dist2 = ray.origin.dst2(position);//ray.origin to object distance works ok
                }

                if ((dist2 < distance || distance < 0f) && dist2 >= 0) {
                    picked = e;
                    distance = dist2;
                }
            }
        }
        return picked;
    }

    /*
     * Intersector.intersectRaySphere() modified to return the "distance" of ray endpoint
     *  to object center ( >= 0 )  or -1 if no intersection
     *  Create an extended Ray class for this.
     */
    private float intersect(Ray ray, Vector3 center, float radius) {

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
