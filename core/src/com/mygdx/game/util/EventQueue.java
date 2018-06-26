package com.mygdx.game.util;

import com.badlogic.ashley.signals.Listener;
import com.badlogic.ashley.signals.Signal;

import java.util.PriorityQueue;

/*
 * https://stackoverflow.com/questions/38208221/libgdx-ashley-framework-ecs-what-is-the-proper-way-of-talking-system-to-anot
 */
public class EventQueue implements Listener<GameEvent> {

    private PriorityQueue<GameEvent> queue;

    public EventQueue() {
        queue = new PriorityQueue<GameEvent>();
    }

    public GameEvent[] getEvents() {
        GameEvent[] events = queue.toArray(new GameEvent[0]);
        queue.clear();
        return events;
    }

    public GameEvent poll() {
        return queue.poll();
    }

    @Override
    public void receive(Signal<GameEvent> signal, GameEvent event) {
        queue.add(event);
    }

}