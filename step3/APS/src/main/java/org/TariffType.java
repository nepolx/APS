package org;

/**
 * Тариф источника/заявки. Приоритет при выборе из буфера (Д2Б4):
 * PREMIUM > BUSINESS > BASIC.
 */
public enum TariffType {
    BASIC(1),
    BUSINESS(2),
    PREMIUM(3);

    private final int priority;

    TariffType(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
