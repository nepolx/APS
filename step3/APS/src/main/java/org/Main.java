package org;

public class Main {
    public static void main(String[] args) {
        Statistics stats = new Statistics();
        EventCalendar calendar = new EventCalendar();

        // буфер на 4 места, чтобы отказы появились быстро
        TicketQueue queue = new TicketQueue(4);

        // три источника по тарифам, интервал между заявками ~ Uniform(1, 3) (ИЗ2)
        Source[] sources = {
                new Source("S1", TariffType.BASIC, 1.0, 3.0),
                new Source("S2", TariffType.BUSINESS, 1.0, 3.0),
                new Source("S3", TariffType.PREMIUM, 1.0, 3.0)
        };

        // два прибора, время обслуживания ~ Exp(0.5), в среднем 2 единицы (ПЗ1)
        SupportAgent[] agents = {
                new SupportAgent("A1", 0.5),
                new SupportAgent("A2", 0.5)
        };

        TicketRouter router = new TicketRouter(queue, stats);
        AssignmentDispatcher dispatcher = new AssignmentDispatcher(queue, agents);

        Simulation simulation = new Simulation(
                100.0,      // endTime: после этого момента новые заявки не генерируются
                20,         // maxGeneratedTickets
                sources,
                calendar,
                queue,
                router,
                dispatcher,
                agents,
                stats
        );

        simulation.setStepMode(true); // пошаговый режим: календарь, приборы, временная диаграмма
        simulation.start();           // в конце выведется таблица статистики (ОР1)
    }
}