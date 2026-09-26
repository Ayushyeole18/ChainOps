package com.supplychainx.ai.util;

public class ProbabilityUtil {

    /**
     * Standard logistic sigmoid function mapping continuous logit z to [0.0, 1.0].
     */
    public static double sigmoid(double z) {
        return 1.0 / (1.0 + Math.exp(-z));
    }

    /**
     * Calculates empirical Bayes smoothed delay probability.
     * Incorporates prior belief (e.g. baseline 15% late delivery rate in logistics).
     */
    public static double calculateDelayProbability(int delayedOrders, int totalOrders, double priorRate, double priorWeight) {
        if (totalOrders <= 0) return priorRate * 100.0;
        // Posterior mean using Beta prior: (late + alpha) / (total + alpha + beta)
        double alpha = priorRate * priorWeight;
        double beta = (1.0 - priorRate) * priorWeight;
        double posterior = (delayedOrders + alpha) / (totalOrders + alpha + beta);
        return Math.min(100.0, Math.max(0.0, posterior * 100.0));
    }

    /**
     * Categorizes delay risk based on calculated percentage.
     */
    public static String classifyDelayRisk(double delayProbabilityPct) {
        if (delayProbabilityPct >= 30.0) {
            return "CRITICAL";
        } else if (delayProbabilityPct >= 15.0) {
            return "MODERATE";
        } else {
            return "LOW";
        }
    }

    /**
     * Computes a normalized supplier reliability score (0 to 100).
     */
    public static double computeReliabilityScore(double delayProbabilityPct, double avgLeadTimeDays) {
        double delayPenalty = delayProbabilityPct * 0.8;
        double leadTimePenalty = Math.min(20.0, Math.max(0.0, (avgLeadTimeDays - 5.0) * 1.5));
        double score = 100.0 - (delayPenalty + leadTimePenalty);
        return Math.round(Math.min(100.0, Math.max(0.0, score)) * 10.0) / 10.0;
    }
}
