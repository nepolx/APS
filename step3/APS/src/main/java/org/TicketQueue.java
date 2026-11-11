package org;


public class TicketQueue {

    private final Ticket[] slots;
    private final int capacity;
    private int queuePtr = 0;

    public TicketQueue(int capacity) {
        this.capacity = capacity;
        this.slots = new Ticket[capacity];
    }


    public int findFreeSlot() {
        for (int i = 0; i < capacity; i++) {
            if (slots[(queuePtr + i) % capacity] == null ) {
                return (queuePtr + i) % capacity;
            }
        }
        return -1;
    }


    public void insert(Ticket ticket, int slot, double now) {
        slots[slot] = ticket;
        ticket.setSlotIndex(slot);
        ticket.setQueuedAt(now);
        queuePtr = (slot + 1) % capacity;
    }


    public Ticket evictAt(double now) {
        Ticket refused = slots[queuePtr];
        if (refused == null) {
            return null;
        }
        refused.markRefused(now);
        slots[queuePtr] = null;
        return refused;
    }

    public Ticket selectHighestPriority() {
        double earliest_queuedAt = 0;
        int highest_slot = -1;
        int highest_priority = 1;
        for (int i = 0; i < capacity; i++) {

            if (slots[i] != null) {
                double current_queuedAt = slots[i].getQueuedAt();
                int current_priority = slots[i].getPriority();

                if (earliest_queuedAt == 0) {
                    earliest_queuedAt = current_queuedAt;
                    highest_slot = i;
                    highest_priority = current_priority;
                } else {
                    if (current_priority > highest_priority) {
                        highest_priority = current_priority;
                        earliest_queuedAt = current_queuedAt;
                        highest_slot = i;
                    } else if (current_priority == highest_priority && current_queuedAt < earliest_queuedAt) {
                        earliest_queuedAt = current_queuedAt;
                        highest_slot = i;
                    }
                }
            }

        }
        if (highest_slot == -1)
            return null;
        return slots[highest_slot];
    }


    public void freeSlot(int slot) {
        slots[slot] = null;
    }


    public boolean isEmpty() {
        for (int i=0; i < capacity; i++) {
            if (slots[i] != null) {
                return false;
            }
        }
        return true;
    }

    public boolean isFull() {
        for (int i=0; i < capacity; i++) {
            if (slots[i] == null) {
                return false;
            }
        }
        return true;
    }

    public int getQueuePtr() {
        return queuePtr;
    }

    public int getCapacity() {
        return capacity;
    }

    public Ticket[] getSlots() {
        return slots;
    }

    public Ticket getSlot(int i) { return slots[i]; }



}
