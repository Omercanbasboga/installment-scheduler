package dev.omercanbasboga.installments.exception;

public class PlanNotFoundException extends RuntimeException {
    public PlanNotFoundException(Long id) {
        super("No payment plan with id " + id);
    }
}
