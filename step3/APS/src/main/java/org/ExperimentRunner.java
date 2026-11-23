package org;

public class ExperimentRunner {

    private static final double T = 1.643;      // для доверительной вероятности 0,9
    private static final double DELTA = 0.1;    // относительная точность 10%
    private static final int N0 = 100;          // начальное число заявок
    private static final int MAX_N = 100000;

    private final int bufferCapacity;
    private final double genMin;
    private final double genMax;
    private final int agentCount;
    private final double serviceLambda;

    public ExperimentRunner(int bufferCapacity, double genMin, double genMax,
                            int agentCount, double serviceLambda) {
        this.bufferCapacity = bufferCapacity;
        this.genMin = genMin;
        this.genMax = genMax;
        this.agentCount = agentCount;
        this.serviceLambda = serviceLambda;
    }

    public Simulation build(int n, boolean stepMode) {
        Statistics stats = new Statistics();
        EventCalendar calendar = new EventCalendar();
        TicketQueue queue = new TicketQueue(bufferCapacity);

        Source[] sources = {
                new Source("S1", TariffType.BASIC, genMin, genMax),
                new Source("S2", TariffType.BUSINESS, genMin, genMax),
                new Source("S3", TariffType.PREMIUM, genMin, genMax)
        };

        SupportAgent[] agents = new SupportAgent[agentCount];
        for (int i = 0; i < agentCount; i++) {
            agents[i] = new SupportAgent("A" + (i + 1), serviceLambda);
        }

        TicketRouter router = new TicketRouter(queue, stats);
        AssignmentDispatcher dispatcher = new AssignmentDispatcher(queue, agents);

        Simulation sim = new Simulation(Double.MAX_VALUE, n, sources, calendar,
                queue, router, dispatcher, agents, stats);
        sim.setStepMode(stepMode);
        return sim;
    }

    public int calcRequiredN(double p) {
        return (int) Math.ceil(T * T * (1 - p) / (DELTA * DELTA * p));
    }


    public void runToAccuracy() {
        int n = N0;
        double pPrev = -1;
        Simulation sim;

        while (true) {
            sim = build(n, false);
            sim.run();
            double p = sim.getRefusalRate();

            // |p0 - p1| < 10% от p0 - точность достигнута
            if (pPrev > 0 && Math.abs(pPrev - p) < DELTA * pPrev) {
                break;
            }
            if (n >= MAX_N) {
                break;
            }

            int next = (p > 0) ? calcRequiredN(p) : n * 2;
            n = Math.min(Math.max(next, N0), MAX_N);
            pPrev = p;
        }
        System.out.println("===РЕЗУЛЬТАТЫ АВТОМАТИЧЕСКОГО РЕЖИМА===");
        System.out.println("Доверительная вероятность: 0.9");
        System.out.println("Относительная точность: 10%");
        System.out.println("Итоговое количество заявок: " + n);
        System.out.println("Фактически создано заявок: " + sim.getGeneratedTickets());
        System.out.printf("Вероятность отказа: %.3f%n", sim.getRefusalRate());
        sim.printResults();
    }
}