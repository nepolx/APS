package org;

import java.util.Optional;

public class AssignmentDispatcher {

    private final TicketQueue queue;
    private final SupportAgent[] agents;
    private int agentPtr = 0;

    public AssignmentDispatcher(TicketQueue queue, SupportAgent[] agents) {
        this.queue = queue;
        this.agents = agents;
    }

    public Optional<AssignmentResult> onQueueNotEmpty(double now) {
        int idx = findFreeAgentIndex();
        if (idx == -1) {
            return Optional.empty();
        }
        Ticket ticket = queue.selectHighestPriority();
        if (ticket == null) {
            return Optional.empty();
        }
        SupportAgent agent = agents[idx];
        queue.freeSlot(ticket.getSlotIndex());
        double duration = agent.process(ticket, now);
        agentPtr = (idx + 1) % agents.length;
        return Optional.of(new AssignmentResult(agent, duration));
    }

    private int findFreeAgentIndex() {
        for (int i = 0; i < agents.length; i++) {
            int idx = (agentPtr + i) % agents.length;
            if (agents[idx].isFree()) {
                return idx;
            }
        }
        return -1;
    }

    public Optional<AssignmentResult> onAgentFreed(SupportAgent agent, double now) {
        return onQueueNotEmpty(now);
    }

    public int getAgentPtr() {
        return agentPtr;
    }
}