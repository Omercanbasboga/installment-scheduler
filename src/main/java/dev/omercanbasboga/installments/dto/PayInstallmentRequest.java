package dev.omercanbasboga.installments.dto;

import java.time.LocalDate;

public class PayInstallmentRequest {

    private LocalDate paidDate;

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
    }
}
