package org;

import java.util.Optional;
import java.util.Scanner;

public class Simulation {

    private double currentTime;
    private final double endTime;
    private final int maxGeneratedTickets;
    private int generatedTickets;
    private boolean stepMode = false;

    private final Scanner scanner = new Scanner(System.in);

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

    public void start() {
        currentTime = 0.0;
        generatedTickets = 0;
        for (Source source : sources) {
            scheduleNextArrival(source);
        }
        runUntilFinished();

        System.out.println();
        System.out.println("Таблица 1. Характеристики источников");
        stats.printStatistics();
        System.out.println();
        System.out.println("Таблица 2. Характеристики приборов");
        printAgentTable();
    }

    public void runNextEvent() {
        if (eventCalendar.isEmpty()) {
            return;
        }
        SimulationEvent event = eventCalendar.nextEvent();
        currentTime = event.time();
        handleEvent(event);
    }

    public void runUntilFinished() {
        while (!eventCalendar.isEmpty()) {
            runNextEvent();
            if (stepMode) {
                printState();
                System.out.println("Нажмите Enter для следующего шага...");
                scanner.nextLine();
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
        Ticket ticket = source.generateTicket(currentTime);
        generatedTickets++;
        if (generatedTickets >= maxGeneratedTickets) {
            eventCalendar.removeArrivals();
        }
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

    public void printState() {
        System.out.println();
        System.out.println("--------------------------------------------------");

        System.out.println("Источники:");
        for (Source s : sources) {
            int generated = stats.getGenerated(s.getId());
            int refused = stats.getRefused(s.getId());
            double percent = generated == 0 ? 0 : 100.0 * refused / generated;
            System.out.printf("  %s: заявок %d, отказов %d, %% отказа %.1f%n",
                    s.getId(), generated, refused, percent);
        }

        System.out.println("Буфер:");
        for (int i = 0; i < queue.getCapacity(); i++) {
            Ticket t = queue.getSlot(i);
            String content = (t == null) ? "пусто" : t.getId() + " (" + t.getTariff() + ")";
            String ptr = (i == queue.getQueuePtr()) ? "   <- QUEUE_PTR" : "";
            System.out.println("  [" + (i + 1) + "] " + content + ptr);
        }

        System.out.println("Приборы:");
        for (int i = 0; i < agents.length; i++) {
            String ptr = (i == dispatcher.getAgentPtr()) ? "   <- AGENT_PTR" : "";
            System.out.println("  " + agents[i].getId() + ": " + (agents[i].isFree() ? "свободен" : "занят") + ptr);
        }
    }

    private void printAgentTable() {
        System.out.printf("%-10s %s%n", "Прибор", "Коэффициент использования");
        for (SupportAgent a : agents) {
            double k = currentTime == 0 ? 0 : a.getBusyTime() / currentTime;
            System.out.printf("%-10s %.3f%n", a.getId(), k);
        }
    }
}