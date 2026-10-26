package org;

public record SimulationEvent(
        double time,
        EventType type,
        SupportAgent agent,
        Source source
) {

}
