package dev.omercanbasboga.installments.dto;

import dev.omercanbasboga.installments.model.PaymentPlan;
import dev.omercanbasboga.installments.model.PlanStatus;
import dev.omercanbasboga.installments.model.SchedulingPolicy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PlanResponse {

    private final Long id;
    private final BigDecimal totalAmount;
    private final LocalDate startDate;
    private final SchedulingPolicy policy;
    private final PlanStatus status;
    private final List<InstallmentResponse> installments;

    public PlanResponse(PaymentPlan plan) {
        this.id = plan.getId();
        this.totalAmount = BigDecimal.valueOf(plan.getTotalAmountCents(), 2);
        this.startDate = plan.getStartDate();
        this.policy = plan.getPolicy();
        this.status = plan.getStatus();
        this.installments = plan.getInstallments().stream()
                .map(InstallmentResponse::new)
                .collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public SchedulingPolicy getPolicy() {
        return policy;
    }

    public PlanStatus getStatus() {
        return status;
    }

    public List<InstallmentResponse> getInstallments() {
        return installments;
    }
}
