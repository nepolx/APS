package org;

import java.util.Random;

public class SupportAgent {
    private final String id;
    private AgentStatus status = AgentStatus.FREE;
    private Ticket currentTicket = null;
    private final double serviceRateLambda;
    private final Random random = new Random();

    public SupportAgent(String id, double serviceRateLambda) {
        this.id = id;
        this.serviceRateLambda = serviceRateLambda;
    }

    public boolean isFree() {
        return status == AgentStatus.FREE;
    }

    public double process(Ticket ticket, double now) {
        status = AgentStatus.BUSY;
        currentTicket = ticket;
        currentTicket.markProcessing(now);
        return serviceTime();
    }

    private double serviceTime() {
        return -Math.log(1 - random.nextDouble()) / serviceRateLambda;
    }

    public Ticket release(double now) {
        Ticket ticket = currentTicket;
        ticket.markDone(now);
        status = AgentStatus.FREE;
        currentTicket = null;
        return ticket;
    }

    public String getId() {
        return id;
    }

}
