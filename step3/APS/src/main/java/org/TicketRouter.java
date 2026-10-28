package org;

/**
 * TicketRouter — Диспетчер постановки.
 *
 * Классический вариант (п.1 памятки): работает ТОЛЬКО с буфером,
 * ничего не знает об операторах. Единственный триггер — приход новой заявки.
 */
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
               queue.insert(ticket, slot, now);           // Д1ОЗ1
           } else {
               int victimSlot = queue.getQueuePtr();
               Ticket victim = queue.evictAt(now);  // Д1ОО1
               stats.recordRefused(victim.getSourceId());
               queue.insert(ticket, victimSlot, now);
           }
    }

    public boolean putTicket(Ticket ticket) {
        /*.*/
        return true;
    }
}
