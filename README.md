# installment-scheduler

A small Spring Boot service that generates and manages installment payment plans, the kind of thing you'd use for a buy-now-pay-later checkout. Split a purchase into N installments, mark them paid, handle someone paying late, or let someone pay off what's left early.

Not a payment processor. No card data, no actual money movement, it just does the scheduling and the math around it.

## Why this exists

I built this after getting a few steps into a BNPL company's hiring process and realizing I'd never actually sat down and worked through the parts of this domain that are easy to get subtly wrong. Splitting a total into equal payments sounds trivial until you try it: split 100.00 into 3 and you get 33.333..., and three payments of 33.33 only add up to 99.99. Somebody has to eat that missing cent, and it should be a deliberate choice, not a rounding accident that leaks money on every single plan you create.

## The rounding

Amounts are stored as cents (a `long`), not `BigDecimal` or `double`, for the whole lifetime of a plan. Doing money math in floating point or repeatedly rounding decimals is how you end up with totals that don't reconcile.

To split the total across N installments, each one first gets its integer-division floor share. Whatever's left over (the leftover cents from the division) gets handed out one cent at a time to the installments that got rounded down the hardest, largest fractional remainder first. This is the same largest remainder method used for seat apportionment, and it guarantees two things: the installments always sum back to exactly the original total, and the rounding is spread out fairly instead of always dumping the extra cent on whoever happens to be first or last.

The same method backs both scheduling policies:
- `EQUAL`: every installment gets equal weight.
- `FRONT_LOADED`: the first installment gets double weight, so it's noticeably bigger than the rest, closer to what a lot of real BNPL products actually do (a bigger first payment, then smaller equal ones after).

## Late fees

An installment can be flagged overdue in two places: a `processOverdue` sweep you'd run daily in a real system, or right when someone tries to pay after the due date has already passed. Either way, the fee only gets applied once. There's a boolean flag on the installment for this, and every place that could apply a fee checks it first. Run the overdue sweep three times a day and a customer isn't charged three late fees for the same missed payment.

## Early payoff

Paying off a plan early adds up whatever's still outstanding (pending installments, plus any overdue ones with their fees already baked in), cancels the individual future installments, and replaces them with a single final payment for that exact remaining balance, due today. The plan's total in vs total out always reconciles: whatever was paid across the lifetime of the plan, plus the payoff amount, equals the original total plus any late fees actually incurred. Nothing more, nothing less.

## Stack

Java 21, Spring Boot 3, Spring Data JPA, H2 (in-memory, no setup needed to run this locally).

## Running it

```bash
./mvnw spring-boot:run
```

Starts on port 8080. H2 console is on `/h2-console` if you want to poke at the data directly.

## API

```
POST /api/plans                                       create a plan
  { "totalAmount": 100.00, "installmentCount": 3, "startDate": "2026-01-01", "policy": "EQUAL" }

GET  /api/plans/{id}                                   view a plan and its installments

POST /api/plans/{id}/installments/{seq}/pay            mark one installment paid
  { "paidDate": "2026-02-01" }   (optional, defaults to today)

POST /api/plans/{id}/process-overdue?asOf=2026-03-01   sweep for overdue installments

POST /api/plans/{id}/payoff?date=2026-02-15            pay off everything remaining
```

## Tests

`ScheduleGeneratorTest` covers the rounding property across a range of installment counts, the classic 33/33/34 split, a single-installment plan, and that `FRONT_LOADED` actually front-loads while still summing correctly.

`PaymentServiceTest` covers the on-time-vs-one-day-late boundary, that running the overdue sweep multiple times doesn't stack the late fee, multiple installments going overdue at once, and early payoff both with and without an outstanding late fee already applied.
