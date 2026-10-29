package org;

import java.util.Arrays;
import java.util.List;

/**
 * ОД3: временная диаграмма текущего состояния (текстом).
 * Строки: источники (И), приборы (П), места буфера (Б).
 * Источник: | и номер заявки в момент генерации.
 * Прибор: [ ... ] интервал обслуживания заявки.
 * Буфер: [ ... ] интервал стояния заявки в слоте (x в конце - заявка получила отказ).
 */
public class TimelineRenderer {

    private static final int WIDTH = 110;

    public void print(double now, List<Ticket> tickets, Source[] sources,
                      SupportAgent[] agents, int bufferCapacity) {
        double scale = Math.min(4.0, WIDTH / Math.max(now, 1.0));
        System.out.printf("--- Временная диаграмма (t = %.2f, 1 символ = %.2f ед. времени) ---%n",
                now, 1.0 / scale);

        // источники
        for (Source s : sources) {
            char[] row = emptyRow();
            for (Ticket t : tickets) {
                if (t.getSourceId().equals(s.getId())) {
                    drawTick(row, col(t.getCreatedAt(), scale), t.getId());
                }
            }
            printRow(s.getId(), row);
        }

        // приборы
        for (SupportAgent a : agents) {
            char[] row = emptyRow();
            for (Ticket t : tickets) {
                if (a.getId().equals(t.getAgentId()) && t.getServiceStartedAt() != null) {
                    double end = (t.getStatus() == TicketStatus.DONE) ? t.getFinishedAt() : now;
                    drawInterval(row, col(t.getServiceStartedAt(), scale), col(end, scale), t.getId());
                }
            }
            printRow(a.getId(), row);
        }

        // буфер (сверху последнее место, как на примере из пособия)
        for (int slot = bufferCapacity - 1; slot >= 0; slot--) {
            char[] row = emptyRow();
            for (Ticket t : tickets) {
                if (t.getQueuedAt() != null && t.getSlotIndex() != null && t.getSlotIndex() == slot) {
                    String label = t.getId() + (t.getStatus() == TicketStatus.REFUSED ? "x" : "");
                    drawInterval(row, col(t.getQueuedAt(), scale), col(leaveTime(t, now), scale), label);
                }
            }
            printRow("Б" + (slot + 1), row);
        }

        // ось времени
        char[] axis = new char[WIDTH + 10];
        char[] nums = new char[WIDTH + 10];
        Arrays.fill(axis, '-');
        Arrays.fill(nums, ' ');
        for (int k = 0; k * 10 <= now; k++) {
            int c = col(k * 10.0, scale);
            axis[c] = '+';
            String s = String.valueOf(k * 10);
            for (int j = 0; j < s.length() && c + j < nums.length; j++) {
                nums[c + j] = s.charAt(j);
            }
        }
        printRow("t", axis);
        printRow("", nums);
    }

    // момент выхода заявки из буфера: отказ, уход на прибор или ещё стоит
    private double leaveTime(Ticket t, double now) {
        if (t.getStatus() == TicketStatus.REFUSED) {
            return t.getFinishedAt();
        }
        if (t.getServiceStartedAt() != null) {
            return t.getServiceStartedAt();
        }
        return now;
    }

    private int col(double time, double scale) {
        return (int) Math.round(time * scale);
    }

    private char[] emptyRow() {
        char[] row = new char[WIDTH + 10];
        Arrays.fill(row, '.');
        return row;
    }

    private void drawTick(char[] row, int at, String label) {
        at = clamp(at, row.length);
        row[at] = '|';
        for (int i = 0; i < label.length() && at + 1 + i < row.length && row[at + 1 + i] == '.'; i++) {
            row[at + 1 + i] = label.charAt(i);
        }
    }

    private void drawInterval(char[] row, int from, int to, String label) {
        from = clamp(from, row.length);
        to = clamp(Math.max(to, from + 1), row.length);
        for (int i = from; i <= to; i++) {
            row[i] = '-';
        }
        row[from] = '[';
        row[to] = ']';
        for (int i = 0; i < label.length() && from + 1 + i < to; i++) {
            row[from + 1 + i] = label.charAt(i);
        }
    }

    private int clamp(int x, int len) {
        return Math.min(Math.max(x, 0), len - 1);
    }

    private void printRow(String name, char[] row) {
        System.out.println(String.format("%-4s ", name) + new String(row));
    }
}