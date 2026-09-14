package dev.omercanbasboga.installments.dto;

import dev.omercanbasboga.installments.model.Installment;
import dev.omercanbasboga.installments.model.InstallmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InstallmentResponse {

    private final int sequenceNumber;
    private final LocalDate dueDate;
    private final BigDecimal amount;
    private final BigDecimal lateFee;
    private final BigDecimal amountDue;
    private final InstallmentStatus status;
    private final LocalDate paidDate;

    public InstallmentResponse(Installment installment) {
        this.sequenceNumber = installment.getSequenceNumber();
        this.dueDate = installment.getDueDate();
        this.amount = centsToAmount(installment.getAmountCents());
        this.lateFee = centsToAmount(installment.getLateFeeCents());
        this.amountDue = centsToAmount(installment.amountDueCents());
        this.status = installment.getStatus();
        this.paidDate = installment.getPaidDate();
    }

    private static BigDecimal centsToAmount(long cents) {
        return BigDecimal.valueOf(cents, 2);
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getLateFee() {
        return lateFee;
    }

    public BigDecimal getAmountDue() {
        return amountDue;
    }

    public InstallmentStatus getStatus() {
        return status;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }
}
