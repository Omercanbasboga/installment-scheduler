package dev.omercanbasboga.installments.repository;

import dev.omercanbasboga.installments.model.PaymentPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentPlanRepository extends JpaRepository<PaymentPlan, Long> {
}
