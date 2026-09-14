package dev.omercanbasboga.installments.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

@Entity
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private PaymentPlan plan;

    private int sequenceNumber;

    private LocalDate dueDate;

    private long amountCents;

    private long lateFeeCents;

    private boolean lateFeeApplied;

    @Enumerated(EnumType.STRING)
    private InstallmentStatus status = InstallmentStatus.PENDING;

    private LocalDate paidDate;

    protected Installment() {
    }

    public Installment(int sequenceNumber, LocalDate dueDate, long amountCents) {
        this.sequenceNumber = sequenceNumber;
        this.dueDate = dueDate;
        this.amountCents = amountCents;
    }

    public Long getId() {
        return id;
    }

    public PaymentPlan getPlan() {
        return plan;
    }

    public void setPlan(PaymentPlan plan) {
        this.plan = plan;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(long amountCents) {
        this.amountCents = amountCents;
    }

    public long getLateFeeCents() {
        return lateFeeCents;
    }

    public boolean isLateFeeApplied() {
        return lateFeeApplied;
    }

    public InstallmentStatus getStatus() {
        return status;
    }

    public void setStatus(InstallmentStatus status) {
        this.status = status;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
    }

    /**
     * Total the customer owes for this installment, including a late fee if one has
     * already been applied. Amount alone is not enough once something is overdue.
     */
    public long amountDueCents() {
        return amountCents + lateFeeCents;
    }

    public boolean isOverdueAsOf(LocalDate date) {
        return status == InstallmentStatus.PENDING && dueDate.isBefore(date);
    }

    public void applyLateFee(long feeCents) {
        if (lateFeeApplied) {
            return;
        }
        this.lateFeeCents = feeCents;
        this.lateFeeApplied = true;
        this.status = InstallmentStatus.OVERDUE;
    }

    public void markPaid(LocalDate date) {
        this.status = InstallmentStatus.PAID;
        this.paidDate = date;
    }

    public void cancel() {
        this.status = InstallmentStatus.CANCELLED;
    }
}
