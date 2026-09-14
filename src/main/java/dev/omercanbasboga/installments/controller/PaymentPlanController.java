package dev.omercanbasboga.installments.controller;

import dev.omercanbasboga.installments.dto.CreatePlanRequest;
import dev.omercanbasboga.installments.dto.PayInstallmentRequest;
import dev.omercanbasboga.installments.dto.PlanResponse;
import dev.omercanbasboga.installments.service.PaymentPlanService;
import dev.omercanbasboga.installments.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/plans")
public class PaymentPlanController {

    private final PaymentPlanService planService;
    private final PaymentService paymentService;

    public PaymentPlanController(PaymentPlanService planService, PaymentService paymentService) {
        this.planService = planService;
        this.paymentService = paymentService;
    }

    @PostMapping
    public PlanResponse create(@Valid @RequestBody CreatePlanRequest request) {
        return new PlanResponse(planService.createPlan(request));
    }

    @GetMapping("/{id}")
    public PlanResponse get(@PathVariable Long id) {
        return new PlanResponse(planService.getPlan(id));
    }

    @PostMapping("/{id}/installments/{sequenceNumber}/pay")
    public PlanResponse pay(@PathVariable Long id,
                             @PathVariable int sequenceNumber,
                             @RequestBody(required = false) PayInstallmentRequest request) {
        LocalDate paidDate = (request != null && request.getPaidDate() != null) ? request.getPaidDate() : LocalDate.now();
        return new PlanResponse(paymentService.payInstallment(id, sequenceNumber, paidDate));
    }

    @PostMapping("/{id}/process-overdue")
    public PlanResponse processOverdue(@PathVariable Long id,
                                        @RequestParam(required = false) LocalDate asOf) {
        LocalDate date = asOf != null ? asOf : LocalDate.now();
        return new PlanResponse(paymentService.processOverdue(id, date));
    }

    @PostMapping("/{id}/payoff")
    public PlanResponse payOff(@PathVariable Long id,
                                @RequestParam(required = false) LocalDate date) {
        LocalDate payoffDate = date != null ? date : LocalDate.now();
        return new PlanResponse(paymentService.payOff(id, payoffDate));
    }
}
