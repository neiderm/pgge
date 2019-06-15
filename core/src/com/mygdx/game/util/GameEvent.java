package com.mygdx.game.util;

import com.badlogic.ashley.core.Entity;

/*
 * Reference:
 *  https://stackoverflow.com/questions/38208221/libgdx-ashley-framework-ecs-what-is-the-proper-way-of-talking-system-to-anot
 */
public class GameEvent implements Comparable<GameEvent> {

    public enum EventType {
        EVT_SEE_OBJECT,
        EVT_HIT_DETECT
    }

    private Entity entity;

    private EventType eventType;
    private Object object;
//    private int id;


    public GameEvent set(EventType t, Object o, int id) {

//        this.id = id;
        this.eventType = t;
        this.object = o;
        return this;
    }

    public Entity getEntity(){

        return entity;
    }

    public void setEntity(Entity e){

        this.entity = e;
    }

    public EventType getEventType(){

        return eventType;
    }

    public Object getObject() {

        return this.object;
    }

    /*
     * optional: client can override this for custom fucntionality
     * for now:
     *   eventEntity: optional - to inform the event owner if the event involved another entity
     *   eventType:  idk
     */
    public void handle(Entity eventEntity, EventType eventType) {

//        this.entity = eventEntity;
//        this.eventType = eventType;
    }

    /* @return  a negative integer, zero, or a positive integer as this object
     *          is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(GameEvent o) {

        if (o.eventType.ordinal() < this.eventType.ordinal()) {
            return -1;
        } else if (o.eventType.ordinal() > this.eventType.ordinal()) {
            return 1;
        }
        return 0;
    }
}

