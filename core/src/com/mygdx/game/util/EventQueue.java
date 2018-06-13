package com.mygdx.game.util;

import com.badlogic.ashley.signals.Listener;
import com.badlogic.ashley.signals.Signal;

import java.util.PriorityQueue;

public class EventQueue implements Listener<GameEvent> {


    public class GameEventSignal{
        public Object userData;
    }


    private PriorityQueue<GameEvent> eventQueue;

    public EventQueue() {
        eventQueue = new PriorityQueue<GameEvent>();
    }

    public GameEvent[] getEvents() {
        GameEvent[] events = eventQueue.toArray(new GameEvent[0]);
        eventQueue.clear();
        return events;
    }

    public GameEvent poll() {
        return eventQueue.poll();
    }

    @Override
    public void receive(Signal<GameEvent> signal, GameEvent event) {
        eventQueue.add(event);
    }

}