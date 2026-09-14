package dev.omercanbasboga.installments.exception;

public class InstallmentNotFoundException extends RuntimeException {
    public InstallmentNotFoundException(Long planId, int sequenceNumber) {
        super("Plan " + planId + " has no installment #" + sequenceNumber);
    }
}
