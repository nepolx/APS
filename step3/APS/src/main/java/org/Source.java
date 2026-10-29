package org;
import java.util.Random;

/**
 * Source — ИБ, бесконечный источник заявок.
 * ИЗ2 — равномерный закон генерации интервалов между заявками.
 */
public class Source {

    private final String id;
    private final TariffType tariff;
    private final double genIntervalMin;
    private final double genIntervalMax;
    private int ticketCounter = 0;
    private final Random random = new Random();

    public Source(String id, TariffType tariff, double genIntervalMin, double genIntervalMax) {
        this.id = id;
        this.tariff = tariff;
        this.genIntervalMin = genIntervalMin;
        this.genIntervalMax = genIntervalMax;
    }

    public Ticket generateTicket(double now) {
        String ticketId = this.id + "." + (++ticketCounter);
        return new Ticket(ticketId, this.id, this.tariff, now);
    }

    /** ИЗ2 — равномерный закон */
    public double nextInterval() {
        return genIntervalMin + random.nextDouble() * (genIntervalMax - genIntervalMin);
    }

    public String getId() {
        return id;
    }

    public TariffType getTariff() {
        return tariff;
    }
}
