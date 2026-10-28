package org;

public class Main {
    public static void main(String[] args) {
        Statistics stats = new Statistics();

        TicketQueue queue = new TicketQueue(10);
        EventCalendar calendar = new EventCalendar();

        Source[] sources = {
                new Source(
                        "S1",
                        TariffType.values()[0],
                        1.0,
                        3.0
                )
        };

        SupportAgent[] agents = {
                new SupportAgent("A1", 0.5),
                new SupportAgent("A2", 0.5)
        };

        TicketRouter router = new TicketRouter(queue, stats);

        AssignmentDispatcher dispatcher =
                new AssignmentDispatcher(queue, agents);


    }
}