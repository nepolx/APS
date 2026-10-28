package org;

import java.util.PriorityQueue;

public class EventCalendar {

    private final PriorityQueue<SimulationEvent> events =
            new PriorityQueue<>(
                    (a, b) -> Double.compare(a.time(), b.time())
            );

    public void schedule(SimulationEvent event) {
        events.add(event);
    }

    public SimulationEvent nextEvent() {
        return events.poll();
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    public int size() {
        return events.size();
    }

    public java.util.List<SimulationEvent> getEvents() {
        java.util.PriorityQueue<SimulationEvent> copy =
                new java.util.PriorityQueue<>(events);

        java.util.List<SimulationEvent> result =
                new java.util.ArrayList<>();

        while (!copy.isEmpty()) {
            result.add(copy.poll());
        }

        return result;
    }
}
