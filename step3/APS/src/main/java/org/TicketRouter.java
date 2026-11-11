package org;


public class TicketRouter {

    private final TicketQueue queue;
    private final Statistics stats;

    public TicketRouter(TicketQueue queue, Statistics stats) {
        this.queue = queue;
        this.stats = stats;
    }

    public void onTicketGenerated(Ticket ticket, double now) {
        int slot = queue.findFreeSlot();
        if (slot != -1) {
               queue.insert(ticket, slot, now);
           } else {
               int victimSlot = queue.getQueuePtr();
               Ticket victim = queue.evictAt(now);
               stats.recordRefused(victim.getSourceId());
               queue.insert(ticket, victimSlot, now);
           }
    }


}
