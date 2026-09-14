package dev.omercanbasboga.installments.service;

import dev.omercanbasboga.installments.dto.CreatePlanRequest;
import dev.omercanbasboga.installments.exception.InvalidPlanRequestException;
import dev.omercanbasboga.installments.exception.PlanNotFoundException;
import dev.omercanbasboga.installments.model.Installment;
import dev.omercanbasboga.installments.model.PaymentPlan;
import dev.omercanbasboga.installments.repository.PaymentPlanRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PaymentPlanService {

    private final PaymentPlanRepository planRepository;
    private final ScheduleGenerator scheduleGenerator;

    public PaymentPlanService(PaymentPlanRepository planRepository, ScheduleGenerator scheduleGenerator) {
        this.planRepository = planRepository;
        this.scheduleGenerator = scheduleGenerator;
    }

    public PaymentPlan createPlan(CreatePlanRequest request) {
        if (request.getTotalAmount() == null || request.getTotalAmount().signum() <= 0) {
            throw new InvalidPlanRequestException("totalAmount must be a positive number");
        }

        long totalCents = request.getTotalAmount()
                .setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        var generated = scheduleGenerator.generate(
                totalCents,
                request.getInstallmentCount(),
                request.getStartDate(),
                request.getPolicy());

        PaymentPlan plan = new PaymentPlan(totalCents, request.getInstallmentCount(), request.getStartDate(), request.getPolicy());
        generated.forEach(g -> plan.addInstallment(new Installment(g.sequenceNumber(), g.dueDate(), g.amountCents())));

        return planRepository.save(plan);
    }

    public PaymentPlan getPlan(Long id) {
        return planRepository.findById(id).orElseThrow(() -> new PlanNotFoundException(id));
    }
}
