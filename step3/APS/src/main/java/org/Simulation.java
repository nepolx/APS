package org;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Simulation {

    private double currentTime;
    private final double endTime;
    private final int maxGeneratedTickets;
    private int generatedTickets;
    private boolean stepMode = false;
    private SimulationEvent lastEvent;

    private final List<String> stepLog = new ArrayList<>();
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

    // Инициализация: первое ARRIVAL от каждого источника, затем главный цикл
    public void start() {
        currentTime = 0.0;
        generatedTickets = 0;
        for (Source source : sources) {
            scheduleNextArrival(source);
        }
        if (stepMode) {
            stepLog.add("Начальное состояние: запланированы первые поступления");
            printState();
            waitForEnter();
        }
        runUntilFinished();
        stats.printStatistics();
    }

    public void runNextEvent() {
        if (eventCalendar.isEmpty()) {
            return;
        }
        stepLog.clear();
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
                waitForEnter();
            }
        }
    }

    private void waitForEnter() {
        System.out.println("Нажмите Enter для следующего шага...");
        scanner.nextLine();
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
            stepLog.add("Лимит заявок достигнут, поступление источника " + source.getId() + " не обрабатывается");
            return;
        }
        Ticket ticket = source.generateTicket(currentTime);
        generatedTickets++;
        stats.recordGenerated(source.getId());
        stepLog.add("Заявка " + ticket.getId() + " сгенерирована источником " + source.getId()
                + " (тариф " + ticket.getTariff() + ")");

        scheduleNextArrival(source);

        int refusedBefore = stats.getTotalRefused();
        router.onTicketGenerated(ticket, currentTime);
        if (stats.getTotalRefused() > refusedBefore) {
            stepLog.add("Буфер полон: выбивание заявки под QUEUE_PTR (Д1ОО1), новая заявка встала на её место");
        } else {
            stepLog.add("Заявка поставлена в буфер (Д1ОЗ1)");
        }

        tryAssign(dispatcher.onQueueNotEmpty(currentTime));
    }

    // SERVICE_COMPLETED: освобождение прибора -> статистика -> триггер диспетчера выбора
    private void handleServiceCompleted(SupportAgent agent) {
        Ticket ticket = agent.release(currentTime);
        stats.recordCompleted(ticket);
        stepLog.add("Прибор " + agent.getId() + " завершил обработку заявки " + ticket.getId());

        tryAssign(dispatcher.onAgentFreed(agent, currentTime));
    }

    private void tryAssign(Optional<AssignmentResult> result) {
        if (result.isPresent()) {
            AssignmentResult a = result.get();
            stepLog.add(String.format(
                    "Заявка выбрана из буфера (Д2Б4) и отправлена на прибор %s (Д2П2), время обслуживания %.3f",
                    a.agent().getId(), a.serviceDuration()));
            scheduleServiceCompletion(a.agent(), currentTime + a.serviceDuration());
        } else {
            stepLog.add("Отправки на прибор нет (все приборы заняты или буфер пуст)");
        }
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

    // ОД1: календарь событий, буфер, текущее состояние (после каждого особого события)
    public void printState() {
        System.out.println();
        System.out.println("==================================================");
        System.out.printf("Модельное время: %.3f%n", currentTime);

        System.out.println("--- Что произошло ---");
        for (String line : stepLog) {
            System.out.println("  " + line);
        }

        System.out.println("--- Календарь событий ---");
        System.out.printf("%-20s %-8s %s%n", "Событие", "Объект", "Время");
        for (SimulationEvent e : eventCalendar.getEvents()) {
            String who = (e.type() == EventType.ARRIVAL) ? e.source().getId() : e.agent().getId();
            System.out.printf("%-20s %-8s %.3f%n", e.type(), who, e.time());
        }

        System.out.println("--- Источники ---");
        System.out.printf("%-8s %-10s %-14s %-8s %s%n", "Источник", "Тариф", "Сгенерировано", "Отказов", "% отказа");
        for (Source s : sources) {
            int generated = stats.getGenerated(s.getId());
            int refused = stats.getRefused(s.getId());
            double percent = generated == 0 ? 0 : 100.0 * refused / generated;
            System.out.printf("%-8s %-10s %-14d %-8d %.1f%n", s.getId(), s.getTariff(), generated, refused, percent);
        }

        System.out.println("--- Буфер ---");
        for (int i = 0; i < queue.getCapacity(); i++) {
            Ticket t = queue.getSlot(i);
            String content = (t == null) ? "пусто" : t.getId() + " (" + t.getTariff() + ")";
            String ptr = (i == queue.getQueuePtr()) ? "   <- QUEUE_PTR" : "";
            System.out.println("  [" + (i + 1) + "] " + content + ptr);
        }

        System.out.println("--- Приборы ---");
        for (int i = 0; i < agents.length; i++) {
            String ptr = (i == dispatcher.getAgentPtr()) ? "   <- AGENT_PTR" : "";
            System.out.println("  " + agents[i].getId() + ": " + (agents[i].isFree() ? "свободен" : "занят") + ptr);
        }

        int totalGenerated = stats.getTotalGenerated();
        int totalRefused = stats.getTotalRefused();
        double totalPercent = totalGenerated == 0 ? 0 : 100.0 * totalRefused / totalGenerated;
        System.out.printf("Всего: сгенерировано %d, отказов %d, %% отказа %.1f%n",
                totalGenerated, totalRefused, totalPercent);
    }
}