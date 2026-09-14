package dev.omercanbasboga.installments.repository;

import dev.omercanbasboga.installments.model.Installment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstallmentRepository extends JpaRepository<Installment, Long> {
}
