package dev.omercanbasboga.installments.service;

import dev.omercanbasboga.installments.model.SchedulingPolicy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Splits a total amount into a list of installments. Kept free of Spring
 * dependencies on purpose (the @Component below is just so PaymentPlanService
 * can have it injected), tests just do `new ScheduleGenerator()` directly.
 *
 * The rounding problem: splitting 100.00 into 3 equal parts gives 33.333...,
 * and three installments of 33.33 only add up to 99.99. Amounts are handled in
 * cents (long) and the leftover cents get handed out one at a time using the
 * largest remainder method, so the installments always sum back to the exact
 * total no matter what policy generated the weights.
 */
@Component
public class ScheduleGenerator {

    public List<GeneratedInstallment> generate(long totalCents, int installmentCount, LocalDate startDate, SchedulingPolicy policy) {
        if (installmentCount < 1) {
            throw new IllegalArgumentException("installmentCount must be at least 1");
        }
        if (totalCents < 1) {
            throw new IllegalArgumentException("totalCents must be positive");
        }

        int[] weights = policy.weights(installmentCount);
        long[] amounts = splitByWeights(totalCents, weights);

        List<GeneratedInstallment> result = new ArrayList<>(installmentCount);
        for (int i = 0; i < installmentCount; i++) {
            LocalDate dueDate = startDate.plusMonths(i);
            result.add(new GeneratedInstallment(i, dueDate, amounts[i]));
        }
        return result;
    }

    /**
     * Largest remainder method. Give every slot its floor share by weight, then
     * hand out whatever cents are left, one each, to the slots that got rounded
     * down the most.
     */
    static long[] splitByWeights(long totalCents, int[] weights) {
        int n = weights.length;
        long totalWeight = 0;
        for (int w : weights) {
            totalWeight += w;
        }

        long[] base = new long[n];
        long[] remainderNumerator = new long[n];
        long allocated = 0;

        for (int i = 0; i < n; i++) {
            long product = totalCents * weights[i];
            base[i] = product / totalWeight;
            remainderNumerator[i] = product % totalWeight;
            allocated += base[i];
        }

        long leftoverCents = totalCents - allocated;

        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
        }
        java.util.Arrays.sort(order, (a, b) -> {
            int cmp = Long.compare(remainderNumerator[b], remainderNumerator[a]);
            if (cmp != 0) {
                return cmp;
            }
            return Integer.compare(a, b);
        });

        for (int i = 0; i < leftoverCents; i++) {
            base[order[i]] += 1;
        }

        return base;
    }

    public record GeneratedInstallment(int sequenceNumber, LocalDate dueDate, long amountCents) {
    }
}
