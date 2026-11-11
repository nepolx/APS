package org;

public class Main {
    public static void main(String[] args) {
        Statistics stats = new Statistics();
        EventCalendar calendar = new EventCalendar();

        TicketQueue queue = new TicketQueue(2);

        Source[] sources = {
                new Source("S1", TariffType.BASIC, 0.5, 1.5),
                new Source("S2", TariffType.BUSINESS, 0.5, 1.5),
                new Source("S3", TariffType.PREMIUM, 0.5, 1.5)
        };

        SupportAgent[] agents = {
                new SupportAgent("A1", 0.5),
                new SupportAgent("A2", 0.5)
        };

        TicketRouter router = new TicketRouter(queue, stats);
        AssignmentDispatcher dispatcher = new AssignmentDispatcher(queue, agents);

        Simulation simulation = new Simulation(
                100.0,
                10,
                sources,
                calendar,
                queue,
                router,
                dispatcher,
                agents,
                stats
        );

        simulation.setStepMode(true);
        simulation.start();
    }
}