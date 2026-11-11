package org;

import java.util.Map;
import java.util.TreeMap;

public class Statistics {

    private final Map<String, Integer> generatedCount = new TreeMap<>();
    private final Map<String, Integer> refusedCount = new TreeMap<>();
    private final Map<String, Integer> completedCount = new TreeMap<>();
    private final Map<String, Double> sumWait = new TreeMap<>();       // ожидание в буфере (TБП)
    private final Map<String, Double> sumService = new TreeMap<>();    // обслуживание (Tобсл)
    private final Map<String, Double> sumWaitSq = new TreeMap<>();     // для дисперсии TБП
    private final Map<String, Double> sumServiceSq = new TreeMap<>();  // для дисперсии Tобсл

    public void recordGenerated(String sourceId) {
        generatedCount.merge(sourceId, 1, Integer::sum);
    }

    public void recordRefused(String sourceId) {
        refusedCount.merge(sourceId, 1, Integer::sum);
    }

    // вызывается при SERVICE_COMPLETED, когда у заявки уже проставлены все метки времени
    public void recordCompleted(Ticket ticket) {
        String id = ticket.getSourceId();
        double wait = ticket.getServiceStartedAt() - ticket.getQueuedAt();
        double service = ticket.getFinishedAt() - ticket.getServiceStartedAt();

        completedCount.merge(id, 1, Integer::sum);
        sumWait.merge(id, wait, Double::sum);
        sumService.merge(id, service, Double::sum);
        sumWaitSq.merge(id, wait * wait, Double::sum);
        sumServiceSq.merge(id, service * service, Double::sum);
    }

    public int getGenerated(String sourceId) {
        return generatedCount.getOrDefault(sourceId, 0);
    }

    public int getRefused(String sourceId) {
        return refusedCount.getOrDefault(sourceId, 0);
    }

    public void printStatistics() {
        System.out.printf("%-10s %-8s %-8s %-8s %-8s %-8s %-8s %s%n",
                "Источник", "Заявок", "pотк", "Tпреб", "TБП", "Tобсл", "ДБП", "Добсл");
        for (String id : generatedCount.keySet()) {
            int generated = generatedCount.get(id);
            int refused = refusedCount.getOrDefault(id, 0);
            int completed = completedCount.getOrDefault(id, 0);

            double pRefuse = (double) refused / generated;
            double avgWait = completed == 0 ? 0 : sumWait.getOrDefault(id, 0.0) / completed;
            double avgService = completed == 0 ? 0 : sumService.getOrDefault(id, 0.0) / completed;
            double varWait = completed == 0 ? 0 : sumWaitSq.getOrDefault(id, 0.0) / completed - avgWait * avgWait;
            double varService = completed == 0 ? 0 : sumServiceSq.getOrDefault(id, 0.0) / completed - avgService * avgService;

            System.out.printf("%-10s %-8d %-8.3f %-8.3f %-8.3f %-8.3f %-8.3f %.3f%n",
                    id, generated, pRefuse, avgWait + avgService, avgWait, avgService, varWait, varService);
        }
    }
}