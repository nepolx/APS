package org;

/**
 * Результат успешного назначения заявки оператору — нужен, чтобы
 * SimulationEngine знал, какому именно агенту и через сколько времени
 * запланировать событие SERVICE_COMPLETED.
 */
public record AssignmentResult(SupportAgent agent, double serviceDuration) {
}