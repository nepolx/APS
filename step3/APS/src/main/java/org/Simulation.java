package org;

import java.util.Optional;

public class Simulation {

    private double currentTime;
    private double endTime;
    private int maxGeneratedTickets;
    private int generatedTickets;

    private Source[] sources;
    private EventCalendar eventCalendar;
    private TicketRouter router;
    private AssignmentDispatcher dispatcher;
    private SupportAgent[] agents;
    private Statistics stats;

    public void start() {
        currentTime = 0.0;
        generatedTickets = 0;
        for (Source source : sources) {
            scheduleNextArrival(source);
        }
        runUntilFinished();
    }

    public void runNextEvent() {
        if (eventCalendar.isEmpty()){
            return;
        }
        SimulationEvent event = eventCalendar.nextEvent();
        currentTime = event.time();
        handleEvent(event);
    }

    public void runUntilFinished() {
        while (!isFinished() && !eventCalendar.isEmpty()) {
            runNextEvent();
        }
    }

    private void handleEvent(SimulationEvent event) {
        if (event.type() == EventType.ARRIVAL) {
            handleTicketGenerated(event.source());
        } else if (event.type() == EventType.SERVICE_COMPLETED) {
            handleServiceCompleted(event.agent());
        }
    }

    private void handleTicketGenerated(Source source) {
        if (maxGeneratedTickets == generatedTickets) {
            return;
        }
        Ticket ticket = source.generateTicket();
        generatedTickets++;
        router.putTicket(ticket);
        scheduleNextArrival(source);
    }

    private void handleServiceCompleted(SupportAgent agent) {
        Ticket ticket = agent.release(currentTime);

        stats.recordCompleted(ticket);

        Optional<AssignmentResult> result =
                dispatcher.onAgentFreed(agent, currentTime);

        result.ifPresent(assignment -> scheduleServiceCompletion(
                assignment.agent(),
                currentTime + assignment.serviceDuration()
        ));
    }


    private void scheduleNextArrival(Source source) {
        if (generatedTickets >= maxGeneratedTickets) {
            return;
        }

        double arrivalTime = currentTime + source.nextInterval();

        if (arrivalTime <= endTime) {
            SimulationEvent event = new SimulationEvent(
                    arrivalTime,
                    EventType.ARRIVAL,
                    null,
                    source
            );

            eventCalendar.schedule(event);
        }
}

    private void scheduleServiceCompletion(SupportAgent agent, double time) {
        SimulationEvent event = new SimulationEvent(
                time,
                EventType.SERVICE_COMPLETED,
                agent,
                null
        );

        eventCalendar.schedule(event);
    }

    private boolean isFinished() {
        if (currentTime >= endTime) {
            return true;
        }

        return generatedTickets >= maxGeneratedTickets
                && eventCalendar.isEmpty();
    }
}