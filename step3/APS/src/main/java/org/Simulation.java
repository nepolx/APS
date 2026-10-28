package org;

import java.util.Optional;

public class Simulation {

    private double currentTime;
    private final double endTime;
    private final int maxGeneratedTickets;
    private int generatedTickets;
    private boolean stepMode = false;
    private SimulationEvent lastEvent;

    private final Source[] sources;
    private final EventCalendar eventCalendar;
    private final TicketQueue queue;
    private final TicketRouter router;
    private final AssignmentDispatcher dispatcher;
    private final SupportAgent[] agents;
    private final Statistics stats;

    public Simulation(
            double endTime,
            int maxGeneratedTickets,
            Source[] sources,
            EventCalendar eventCalendar,
            TicketQueue queue,
            TicketRouter router,
            AssignmentDispatcher dispatcher,
            SupportAgent[] agents,
            Statistics stats
    ) {
        this.endTime = endTime;
        this.maxGeneratedTickets = maxGeneratedTickets;
        this.sources = sources;
        this.eventCalendar = eventCalendar;
        this.queue = queue;
        this.router = router;
        this.dispatcher = dispatcher;
        this.agents = agents;
        this.stats = stats;
    }

    public void setStepMode(boolean stepMode) {
        this.stepMode = stepMode;
    }

    // Инициализация: первое ARRIVAL от каждого источника, затем главный цикл
    public void start() {
        currentTime = 0.0;
        generatedTickets = 0;
        for (Source source : sources) {
            scheduleNextArrival(source);
        }
        runUntilFinished();
        stats.printStatistics();
    }

    public void runNextEvent() {
        if (eventCalendar.isEmpty()) {
            return;
        }
        SimulationEvent event = eventCalendar.nextEvent();
        currentTime = event.time();
        lastEvent = event;
        handleEvent(event);
    }

    // Генерация прекращается сама (см. scheduleNextArrival),
    // обслуживание доводится до ухода последней заявки
    public void runUntilFinished() {
        while (!eventCalendar.isEmpty()) {
            runNextEvent();
            if (stepMode) {
                printState();
            }
        }
    }

    private void handleEvent(SimulationEvent event) {
        if (event.type() == EventType.ARRIVAL) {
            handleArrival(event.source());
        } else if (event.type() == EventType.SERVICE_COMPLETED) {
            handleServiceCompleted(event.agent());
        }
    }

    // ARRIVAL: генерация -> статистика -> следующее поступление -> постановка в буфер -> триггер диспетчера выбора
    private void handleArrival(Source source) {
        if (generatedTickets >= maxGeneratedTickets) {
            return;
        }
        Ticket ticket = source.generateTicket(currentTime);
        generatedTickets++;
        stats.recordGenerated(source.getId());
        scheduleNextArrival(source);
        router.onTicketGenerated(ticket, currentTime);
        tryAssign(dispatcher.onQueueNotEmpty(currentTime));
    }

    // SERVICE_COMPLETED: освобождение прибора -> статистика -> триггер диспетчера выбора
    private void handleServiceCompleted(SupportAgent agent) {
        Ticket ticket = agent.release(currentTime);
        stats.recordCompleted(ticket);
        tryAssign(dispatcher.onAgentFreed(agent, currentTime));
    }

    private void tryAssign(Optional<AssignmentResult> result) {
        result.ifPresent(a -> scheduleServiceCompletion(
                a.agent(),
                currentTime + a.serviceDuration()
        ));
    }

    private void scheduleNextArrival(Source source) {
        if (generatedTickets >= maxGeneratedTickets) {
            return;
        }
        double arrivalTime = currentTime + source.nextInterval();
        if (arrivalTime <= endTime) {
            eventCalendar.schedule(new SimulationEvent(
                    arrivalTime,
                    EventType.ARRIVAL,
                    null,
                    source
            ));
        }
    }

    private void scheduleServiceCompletion(SupportAgent agent, double time) {
        eventCalendar.schedule(new SimulationEvent(
                time,
                EventType.SERVICE_COMPLETED,
                agent,
                null
        ));
    }

    // ОД: состояние системы после каждого особого события
    public void printState() {
        System.out.println("==============================");
        System.out.printf("Время: %.3f%n", currentTime);
        if (lastEvent != null) {
            String who = lastEvent.type() == EventType.ARRIVAL
                    ? lastEvent.source().getId()
                    : lastEvent.agent().getId();
            System.out.println("Обработано событие: " + lastEvent.type() + " (" + who + ")");
        }

        System.out.println("--- Календарь событий ---");
        for (SimulationEvent e : eventCalendar.getEvents()) {
            String who = e.type() == EventType.ARRIVAL
                    ? e.source().getId()
                    : e.agent().getId();
            System.out.printf("%-18s %-6s t=%.3f%n", e.type(), who, e.time());
        }

        /*System.out.println("--- Буфер ---");
        for (int i = 0; i < queue.getCapacity(); i++) {
            Ticket t = queue.getSlot(i);
            String content = (t == null) ? "пусто" : t.getId() + " " + t.getTariff();
            String ptr = (i == queue.getQueuePtr()) ? "  <- QUEUE_PTR" : "";
            System.out.println("[" + i + "] " + content + ptr);
        }*/

        System.out.println("--- Приборы ---");
        for (int i = 0; i < agents.length; i++) {
            String ptr = (i == dispatcher.getAgentPtr()) ? "  <- AGENT_PTR" : "";
            System.out.println(agents[i].getId() + ": " + (agents[i].isFree() ? "свободен" : "занят") + ptr);
        }
        //дописать
        System.out.println("Сгенерировано: " + stats.getTotalGenerated()
                + ", отказов: " + stats.getTotalRefused());
    }
}