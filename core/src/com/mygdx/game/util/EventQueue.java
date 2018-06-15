package com.mygdx.game.util;

import com.badlogic.ashley.signals.Listener;
import com.badlogic.ashley.signals.Signal;

import java.util.PriorityQueue;

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