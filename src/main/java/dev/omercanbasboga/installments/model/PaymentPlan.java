package dev.omercanbasboga.installments.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PaymentPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long totalAmountCents;

    private int installmentCount;

    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    private SchedulingPolicy policy;

    @Enumerated(EnumType.STRING)
    private PlanStatus status = PlanStatus.ACTIVE;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceNumber ASC")
    private List<Installment> installments = new ArrayList<>();

    protected PaymentPlan() {
    }

    public PaymentPlan(long totalAmountCents, int installmentCount, LocalDate startDate, SchedulingPolicy policy) {
        this.totalAmountCents = totalAmountCents;
        this.installmentCount = installmentCount;
        this.startDate = startDate;
        this.policy = policy;
    }

    public Long getId() {
        return id;
    }

    public long getTotalAmountCents() {
        return totalAmountCents;
    }

    public int getInstallmentCount() {
        return installmentCount;
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

    public void setStatus(PlanStatus status) {
        this.status = status;
    }

    public List<Installment> getInstallments() {
        return installments;
    }

    public void addInstallment(Installment installment) {
        installments.add(installment);
        installment.setPlan(this);
    }
}
