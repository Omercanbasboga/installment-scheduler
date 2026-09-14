package dev.omercanbasboga.installments.service;

import dev.omercanbasboga.installments.model.SchedulingPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScheduleGeneratorTest {

    private final ScheduleGenerator generator = new ScheduleGenerator();

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 7, 12})
    void equalSplitAlwaysSumsToTheOriginalTotal(int installmentCount) {
        long totalCents = 10_000;

        var installments = generator.generate(totalCents, installmentCount, LocalDate.of(2026, 1, 1), SchedulingPolicy.EQUAL);

        long sum = installments.stream().mapToLong(ScheduleGenerator.GeneratedInstallment::amountCents).sum();
        assertThat(sum).isEqualTo(totalCents);
    }

    @Test
    void splittingOneHundredIntoThreeGivesTheClassicThirtyThreeThirtyThreeThirtyFour() {
        var installments = generator.generate(10_000, 3, LocalDate.of(2026, 1, 1), SchedulingPolicy.EQUAL);

        var amounts = installments.stream().map(ScheduleGenerator.GeneratedInstallment::amountCents).toList();
        assertThat(amounts).containsExactlyInAnyOrder(3334L, 3333L, 3333L);
        assertThat(amounts.stream().mapToLong(Long::longValue).sum()).isEqualTo(10_000);
    }

    @Test
    void singleInstallmentPlanIsJustTheWholeAmount() {
        var installments = generator.generate(9_999, 1, LocalDate.of(2026, 3, 5), SchedulingPolicy.EQUAL);

        assertThat(installments).hasSize(1);
        assertThat(installments.get(0).amountCents()).isEqualTo(9_999);
    }

    @Test
    void frontLoadedGivesTheFirstInstallmentTheBiggerShareButStillSumsCorrectly() {
        var installments = generator.generate(10_000, 4, LocalDate.of(2026, 1, 1), SchedulingPolicy.FRONT_LOADED);

        long first = installments.get(0).amountCents();
        long second = installments.get(1).amountCents();
        assertThat(first).isGreaterThan(second);

        long sum = installments.stream().mapToLong(ScheduleGenerator.GeneratedInstallment::amountCents).sum();
        assertThat(sum).isEqualTo(10_000);
    }

    @Test
    void dueDatesAreOneMonthApart() {
        var installments = generator.generate(10_000, 3, LocalDate.of(2026, 1, 31), SchedulingPolicy.EQUAL);

        assertThat(installments.get(0).dueDate()).isEqualTo(LocalDate.of(2026, 1, 31));
        assertThat(installments.get(1).dueDate()).isEqualTo(LocalDate.of(2026, 2, 28));
        assertThat(installments.get(2).dueDate()).isEqualTo(LocalDate.of(2026, 3, 31));
    }

    @Test
    void rejectsZeroOrFewerInstallments() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> generator.generate(10_000, 0, LocalDate.now(), SchedulingPolicy.EQUAL));
    }
}
