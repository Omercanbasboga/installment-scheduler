package dev.omercanbasboga.installments.dto;

import dev.omercanbasboga.installments.model.SchedulingPolicy;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreatePlanRequest {

    @NotNull
    private BigDecimal totalAmount;

    @Min(1)
    private int installmentCount;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private SchedulingPolicy policy;

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getInstallmentCount() {
        return installmentCount;
    }

    public void setInstallmentCount(int installmentCount) {
        this.installmentCount = installmentCount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public SchedulingPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(SchedulingPolicy policy) {
        this.policy = policy;
    }
}
