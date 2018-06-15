package com.mygdx.game.util;

import com.badlogic.ashley.core.Entity;

/*
 * Reference:
 *  https://stackoverflow.com/questions/38208221/libgdx-ashley-framework-ecs-what-is-the-proper-way-of-talking-system-to-anot
 */
public class GameEvent implements Comparable<GameEvent> {

    public enum EventType {
        THIS,
        THAT
    }

    public EventType type = EventType.THIS;
    public Entity entity;
    public Object object;


    public GameEvent(Entity e, EventType t, Object o) {
        set(e, t, o);
    }

    public void set(Entity e, EventType t, Object o) {
        this.entity = e;
        this.type = t;
        this.object = o;
    }


    /*
     * optional: client can override this for custom fucntionality
     */
    public void callback(Entity picked) {
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
