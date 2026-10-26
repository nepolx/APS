package org;

/**
 * TicketQueue — буфер (кольцевая очередь фиксированного размера).
 *
 * Соответствует классу TicketQueue на диаграмме классов.
 *
 * Д1ОЗ1 — постановка "по кольцу" от указателя queuePtr.
 * Д1ОО1 — если findFreeSlot() не нашёл места, вытесняется заявка
 *         под текущим положением указателя.
 * Д2Б4  — выбор ОДНОЙ заявки с наивысшим приоритетом источника,
 *         БЕЗ обхода по кольцу (просто максимум среди занятых слотов),
 *
 */
public class TicketQueue {

    private final Ticket[] slots;
    private final int capacity;
    private int queuePtr = 0; // единственный указатель буфера (Д1ОЗ1 / Д1ОО1)

    public TicketQueue(int capacity) {
        this.capacity = capacity;
        this.slots = new Ticket[capacity];
    }

    // ------------------------------------------------------------------
    // Постановка (Д1ОЗ1)
    // ------------------------------------------------------------------


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

    // ------------------------------------------------------------------
    // Отказ / вытеснение (Д1ОО1) — вызывается ТОЛЬКО если findFreeSlot() == -1
    // ------------------------------------------------------------------


    public Ticket evictAt(double now) {
        Ticket refused = slots[queuePtr];
        if (refused == null) {
            return null;
        }
        refused.markRefused(now);
        slots[queuePtr] = null;
        return refused;
    }

    // ------------------------------------------------------------------
    // Выборка (Д2Б4)
    // ------------------------------------------------------------------

    /**
     * TODO: пройти по всем занятым слотам (НЕ по кольцу — просто перебор),
     * вернуть заявку с максимальным ticket.getPriority().
     * При равенстве приоритета — более старую заявку (меньший queuedAt).
     * Если буфер пуст — вернуть null.
     * ВАЖНО: этот метод только НАХОДИТ заявку, не удаляет её из буфера —
     * слот освобождается отдельным вызовом freeSlot() (см. ниже).
     */
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

    /**
     * TODO: slots[slot] = null — вызывается после того, как
     * выбранная заявка физически передана на прибор.
     */
    public void freeSlot(int slot) {
        slots[slot] = null;
    }

    // ------------------------------------------------------------------
    // Проверки состояния
    // ------------------------------------------------------------------

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

    // ------------------------------------------------------------------
    // Для пошагового режима — календарь событий (п.3 памятки):
    // нужно отображать положение указателя на каждой итерации.
    // ------------------------------------------------------------------


}
