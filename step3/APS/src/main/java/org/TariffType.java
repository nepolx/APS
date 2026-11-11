package org;

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
