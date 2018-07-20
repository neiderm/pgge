package com.mygdx.game.util;

import com.badlogic.ashley.core.Entity;

/*
 * Reference:
 *  https://stackoverflow.com/questions/38208221/libgdx-ashley-framework-ecs-what-is-the-proper-way-of-talking-system-to-anot
 */
public class GameEvent implements Comparable<GameEvent> {

    public enum EventType {
        RAY_DETECT,
        RAY_PICK
    }

    public EventType type;
    public Entity entity;
    public Object object;
    public int id;


    public GameEvent(EventType t) {
        this.type = t;
    }


    public GameEvent set(EventType t, Object o, int id) {

        this.id = id;
        this.type = t;
        this.object = o;
        return this;
    }


    /*
     * optional: client can override this for custom fucntionality
     */
    public void callback(Entity picked, EventType eventType) {
        //empty
    }


    /* @return  a negative integer, zero, or a positive integer as this object
    *          is less than, equal to, or greater than the specified object.
   */
    @Override
    public int compareTo(GameEvent o) {

        if (o.type.ordinal() < this.type.ordinal()) {
            return -1;
        } else if (o.type.ordinal() > this.type.ordinal()) {
            return 1;
        }
        return 0;
    }
}

