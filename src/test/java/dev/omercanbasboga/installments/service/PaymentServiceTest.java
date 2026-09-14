package dev.omercanbasboga.installments.service;

import dev.omercanbasboga.installments.config.LateFeeProperties;
import dev.omercanbasboga.installments.model.Installment;
import dev.omercanbasboga.installments.model.InstallmentStatus;
import dev.omercanbasboga.installments.model.PaymentPlan;
import dev.omercanbasboga.installments.model.PlanStatus;
import dev.omercanbasboga.installments.model.SchedulingPolicy;
import dev.omercanbasboga.installments.repository.PaymentPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    private PaymentPlanRepository planRepository;
    private PaymentService paymentService;
    private static final long LATE_FEE_CENTS = 500;

    @BeforeEach
    void setUp() {
        planRepository = mock(PaymentPlanRepository.class);
        when(planRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LateFeeProperties lateFeeProperties = new LateFeeProperties();
        lateFeeProperties.setAmountCents(LATE_FEE_CENTS);

        paymentService = new PaymentService(planRepository, lateFeeProperties);
    }

    private PaymentPlan threeInstallmentPlan() {
        PaymentPlan plan = new PaymentPlan(9_000, 3, LocalDate.of(2026, 1, 1), SchedulingPolicy.EQUAL);
        plan.addInstallment(new Installment(0, LocalDate.of(2026, 1, 1), 3_000));
        plan.addInstallment(new Installment(1, LocalDate.of(2026, 2, 1), 3_000));
        plan.addInstallment(new Installment(2, LocalDate.of(2026, 3, 1), 3_000));
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        return plan;
    }

    @Test
    void payingOnTheDueDateItselfDoesNotCountAsLate() {
        PaymentPlan plan = threeInstallmentPlan();

        paymentService.payInstallment(1L, 0, LocalDate.of(2026, 1, 1));

        Installment first = plan.getInstallments().get(0);
        assertThat(first.getStatus()).isEqualTo(InstallmentStatus.PAID);
        assertThat(first.isLateFeeApplied()).isFalse();
        assertThat(first.amountDueCents()).isEqualTo(3_000);
    }

    @Test
    void payingOneDayAfterTheDueDateAppliesTheLateFee() {
        PaymentPlan plan = threeInstallmentPlan();

        paymentService.payInstallment(1L, 0, LocalDate.of(2026, 1, 2));

        Installment first = plan.getInstallments().get(0);
        assertThat(first.getStatus()).isEqualTo(InstallmentStatus.PAID);
        assertThat(first.isLateFeeApplied()).isTrue();
        assertThat(first.amountDueCents()).isEqualTo(3_000 + LATE_FEE_CENTS);
    }

    @Test
    void processOverdueFlagsEachLateInstallmentWithExactlyOneFee() {
        PaymentPlan plan = threeInstallmentPlan();

        paymentService.processOverdue(1L, LocalDate.of(2026, 3, 15));

        Installment firstDue = plan.getInstallments().get(0);
        Installment secondDue = plan.getInstallments().get(1);
        Installment thirdDue = plan.getInstallments().get(2);

        assertThat(firstDue.getStatus()).isEqualTo(InstallmentStatus.OVERDUE);
        assertThat(secondDue.getStatus()).isEqualTo(InstallmentStatus.OVERDUE);
        assertThat(thirdDue.getStatus()).isEqualTo(InstallmentStatus.OVERDUE);

        long totalLateFees = plan.getInstallments().stream().mapToLong(Installment::getLateFeeCents).sum();
        assertThat(totalLateFees).isEqualTo(3 * LATE_FEE_CENTS);
    }

    @Test
    void runningProcessOverdueTwiceDoesNotDoubleTheLateFee() {
        PaymentPlan plan = threeInstallmentPlan();

        paymentService.processOverdue(1L, LocalDate.of(2026, 1, 15));
        paymentService.processOverdue(1L, LocalDate.of(2026, 1, 20));
        paymentService.processOverdue(1L, LocalDate.of(2026, 1, 25));

        Installment first = plan.getInstallments().get(0);
        assertThat(first.getLateFeeCents()).isEqualTo(LATE_FEE_CENTS);
    }

    @Test
    void payOffCollapsesRemainingInstallmentsIntoOnePaymentForTheExactRemainingBalance() {
        PaymentPlan plan = threeInstallmentPlan();
        paymentService.payInstallment(1L, 0, LocalDate.of(2026, 1, 1));

        paymentService.payOff(1L, LocalDate.of(2026, 1, 20));

        assertThat(plan.getStatus()).isEqualTo(PlanStatus.PAID_OFF);
        assertThat(plan.getInstallments().get(1).getStatus()).isEqualTo(InstallmentStatus.CANCELLED);
        assertThat(plan.getInstallments().get(2).getStatus()).isEqualTo(InstallmentStatus.CANCELLED);

        Installment payoffInstallment = plan.getInstallments().get(plan.getInstallments().size() - 1);
        assertThat(payoffInstallment.getStatus()).isEqualTo(InstallmentStatus.PAID);
        assertThat(payoffInstallment.getAmountCents()).isEqualTo(6_000);
    }

    @Test
    void payOffAfterALateFeeIncludesTheFeeInTheFinalPayment() {
        PaymentPlan plan = threeInstallmentPlan();
        paymentService.processOverdue(1L, LocalDate.of(2026, 1, 15));

        paymentService.payOff(1L, LocalDate.of(2026, 1, 20));

        Installment payoffInstallment = plan.getInstallments().get(plan.getInstallments().size() - 1);
        assertThat(payoffInstallment.getAmountCents()).isEqualTo(9_000 + LATE_FEE_CENTS);
    }
}
