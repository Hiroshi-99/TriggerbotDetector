package org.katsuki.triggerbotdetector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerStats {
    private final UUID playerId;
    private int interactionCount;
    private int attackCount;
    private long lastInteractionTime;
    private long lastAttackTime;
    private long totalInteractionInterval;
    private long totalAttackInterval;
    private final List<Long> attackIntervals = new ArrayList<>();

    public PlayerStats(UUID playerId) {
        this.playerId = playerId;
    }

    public void recordInteraction() {
        long currentTime = System.currentTimeMillis();
        if (lastInteractionTime != 0) {
            totalInteractionInterval += (currentTime - lastInteractionTime);
        }
        lastInteractionTime = currentTime;
        interactionCount++;
    }

    public void recordAttack() {
        long currentTime = System.currentTimeMillis();
        if (lastAttackTime != 0) {
            long interval = currentTime - lastAttackTime;
            attackIntervals.add(interval);
            totalAttackInterval += interval;
        }
        lastAttackTime = currentTime;
        attackCount++;
    }

    public boolean isSuspicious(int maxInterval, int minCount, double maxStdDeviation, double tolerancePercentage) {
        if (attackCount >= minCount) {
            long averageAttackInterval = totalAttackInterval / attackCount;
            double variance = 0.0;
            for (long interval : attackIntervals) {
                variance += Math.pow(interval - averageAttackInterval, 2);
            }
            variance /= attackIntervals.size();
            double stdDeviation = Math.sqrt(variance);

            if (averageAttackInterval < maxInterval && stdDeviation < maxStdDeviation) {
                int consistentFastClicks = (int) attackIntervals.stream().filter(i -> i < maxInterval).count();
                double consistencyRate = (double) consistentFastClicks / attackCount;
                return consistencyRate > tolerancePercentage;
            }
        }
        return false;
    }

    public long getLastInteractionTime() {
        return lastInteractionTime;
    }
}