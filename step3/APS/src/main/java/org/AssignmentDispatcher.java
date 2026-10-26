package org;

import java.util.Optional;

public class AssignmentDispatcher {

    private final TicketQueue queue;
    private final SupportAgent[] agents;
    private int agentPtr = 0;
    private final Statistics stats;

    public AssignmentDispatcher(TicketQueue queue, SupportAgent[] agents, Statistics stats) {
        this.queue = queue;
        this.agents = agents;
        this.stats = stats;
    }

    public Optional<AssignmentResult> onQueueNotEmpty(double now) {
        SupportAgent agent = findFreeAgent();

        if (agent == null) {
            return Optional.empty();
        }

        Ticket ticket = queue.selectHighestPriority();

        if (ticket == null) {
            return Optional.empty();
        }

        return Optional.of(assign(ticket, agent, now));
    }

    private AssignmentResult assign(Ticket ticket, SupportAgent agent, double now) {
        queue.freeSlot(ticket.getSlotIndex());

        double duration = agent.process(ticket, now);

        advanceAgentPtr(agent);

        return new AssignmentResult(agent, duration);
    }

    private void advanceAgentPtr(SupportAgent agent) {
        agentPtr = (agentPtr + 1) % agents.length;
    }

    private SupportAgent findFreeAgent() {
        for (int i = 0; i < agents.length; i++) {
            SupportAgent agent = agents[(agentPtr + i) % agents.length];

            if (agent.isFree()) {
                return agent;
            }
        }

        return null;
    }

    public Optional<AssignmentResult> onAgentFreed(SupportAgent agent, double now) {
        return onQueueNotEmpty(now);
    }
}