package dev.omercanbasboga.installments.service;

import dev.omercanbasboga.installments.config.LateFeeProperties;
import dev.omercanbasboga.installments.exception.InstallmentNotFoundException;
import dev.omercanbasboga.installments.exception.PlanNotFoundException;
import dev.omercanbasboga.installments.model.Installment;
import dev.omercanbasboga.installments.model.InstallmentStatus;
import dev.omercanbasboga.installments.model.PaymentPlan;
import dev.omercanbasboga.installments.model.PlanStatus;
import dev.omercanbasboga.installments.repository.PaymentPlanRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentPlanRepository planRepository;
    private final LateFeeProperties lateFeeProperties;

    public PaymentService(PaymentPlanRepository planRepository, LateFeeProperties lateFeeProperties) {
        this.planRepository = planRepository;
        this.lateFeeProperties = lateFeeProperties;
    }

    /**
     * Marks one installment as paid. If it is being paid after its due date and
     * nothing has flagged it as overdue yet (nobody ran processOverdue since it
     * fell behind), the late fee is applied here instead, so a customer cannot
     * dodge the fee just by paying before the next overdue sweep.
     */
    public PaymentPlan payInstallment(Long planId, int sequenceNumber, LocalDate paidDate) {
        PaymentPlan plan = getPlanOrThrow(planId);
        Installment installment = findInstallment(plan, sequenceNumber);

        if (installment.getStatus() == InstallmentStatus.PENDING && paidDate.isAfter(installment.getDueDate())) {
            installment.applyLateFee(lateFeeProperties.getAmountCents());
        }

        installment.markPaid(paidDate);
        maybeClosePlan(plan);
        return planRepository.save(plan);
    }

    /**
     * Sweeps every plan's pending installments and flags anything whose due
     * date has passed as overdue, adding the configured late fee. Safe to call
     * as often as you like, an installment only ever gets the fee once because
     * applyLateFee no-ops if it has already been applied.
     */
    public PaymentPlan processOverdue(Long planId, LocalDate asOf) {
        PaymentPlan plan = getPlanOrThrow(planId);
        for (Installment installment : plan.getInstallments()) {
            if (installment.isOverdueAsOf(asOf)) {
                installment.applyLateFee(lateFeeProperties.getAmountCents());
            }
        }
        return planRepository.save(plan);
    }

    /**
     * Collapses everything still owed (whatever is PENDING or OVERDUE, late
     * fees included) into a single payment due today, and cancels the future
     * installments that would otherwise still be sitting there unpaid.
     */
    public PaymentPlan payOff(Long planId, LocalDate payoffDate) {
        PaymentPlan plan = getPlanOrThrow(planId);
        List<Installment> outstanding = plan.getInstallments().stream()
                .filter(i -> i.getStatus() == InstallmentStatus.PENDING || i.getStatus() == InstallmentStatus.OVERDUE)
                .toList();

        if (outstanding.isEmpty()) {
            return plan;
        }

        long remainingCents = outstanding.stream().mapToLong(Installment::amountDueCents).sum();

        for (Installment installment : outstanding) {
            installment.cancel();
        }

        int nextSequence = plan.getInstallments().stream()
                .mapToInt(Installment::getSequenceNumber)
                .max()
                .orElse(-1) + 1;

        Installment payoffInstallment = new Installment(nextSequence, payoffDate, remainingCents);
        plan.addInstallment(payoffInstallment);
        payoffInstallment.markPaid(payoffDate);

        plan.setStatus(PlanStatus.PAID_OFF);
        return planRepository.save(plan);
    }

    private void maybeClosePlan(PaymentPlan plan) {
        boolean allSettled = plan.getInstallments().stream()
                .allMatch(i -> i.getStatus() == InstallmentStatus.PAID || i.getStatus() == InstallmentStatus.CANCELLED);
        if (allSettled) {
            plan.setStatus(PlanStatus.PAID_OFF);
        }
    }

    private PaymentPlan getPlanOrThrow(Long planId) {
        return planRepository.findById(planId).orElseThrow(() -> new PlanNotFoundException(planId));
    }

    private Installment findInstallment(PaymentPlan plan, int sequenceNumber) {
        return plan.getInstallments().stream()
                .filter(i -> i.getSequenceNumber() == sequenceNumber)
                .findFirst()
                .orElseThrow(() -> new InstallmentNotFoundException(plan.getId(), sequenceNumber));
    }
}
