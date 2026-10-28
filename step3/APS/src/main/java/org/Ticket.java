package org;

public class Ticket {
    private final String id;
    private final String sourceId;
    private final TariffType tariff;
    private final double createdAt;          // модельное время генерации (ИЗ2)

    private TicketStatus status = TicketStatus.WAITING;
    private Integer slotIndex = null;         // позиция в буфере (для отображения указателей

    // метки времени  нужны для статистики (ожидание/обслуживание/в системе), ОР1
    private Double queuedAt = null;
    private Double serviceStartedAt = null;
    private Double finishedAt = null;

    public Ticket(String id, String sourceId, TariffType tariff, double createdAt) {
        this.id = id;
        this.sourceId = sourceId;
        this.tariff = tariff;
        this.createdAt = createdAt;
    }

    public int getPriority() {
        return tariff.getPriority();
    }

    public void markProcessing(double now) {
        this.status = TicketStatus.PROCESSING;
        this.serviceStartedAt = now;
    }

    public void markDone(double now) {
        this.status = TicketStatus.DONE;
        this.finishedAt = now;
    }

    public void markRefused(double now) {
        this.status = TicketStatus.REFUSED;
        this.finishedAt = now;
    }

    public String getId() {
        return id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public TariffType getTariff() {
        return tariff;
    }

    public Integer getSlotIndex() {
        return slotIndex;
    }

    public void setSlotIndex(Integer slotIndex) {
        this.slotIndex = slotIndex;
    }

    public Double getQueuedAt() {
        return queuedAt;
    }

    public void setQueuedAt(Double queuedAt) {
        this.queuedAt = queuedAt;
    }

    public Double getServiceStartedAt() {
        return serviceStartedAt;
    }

    public Double getFinishedAt() {
        return finishedAt;
    }

    @Override
    public String toString() {
        return "Ticket{id='" + id + "', tariff=" + tariff + ", status=" + status + "}";
    }

}
