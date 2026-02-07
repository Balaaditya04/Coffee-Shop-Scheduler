# Coffee Shop Barista Dilemma — Complete Solution

## Problem Summary
Design an order scheduling system for a coffee shop with 3 baristas that minimizes wait times (<10 min), balances workload, and maintains fairness.

---

## Priority Formula (Exact Specification)

```
Priority = (0.40 × WaitTimeScore) + (0.25 × ComplexityScore) 
         + (0.10 × LoyaltyScore) + (0.25 × UrgencyScore)
```

All scores normalized to **0-100 range**, resulting in final priority **0-100**.

### Score Calculations

| Factor | Weight | Calculation |
|--------|--------|-------------|
| **WaitTimeScore** | 40% | `(waitMinutes / 10) × 100` (capped at 100) |
| **ComplexityScore** | 25% | `((8 - prepMinutes) / 6) × 100` (shorter prep = higher) |
| **LoyaltyScore** | 10% | Regular: `50 + (tier × 10)`, New: `tier × 10` |
| **UrgencyScore** | 25% | See urgency table below |

### Urgency Score Table

| Wait Time | Urgency Score |
|-----------|---------------|
| 0-6 min | `(wait/6) × 25` (0-25) |
| 6-8 min | `25 + ((wait-6)/2 × 50)` (25-75) |
| 8-9 min | `75 + ((wait-8) × 25)` (75-100) |
| ≥9 min | 100 (maximum) |

---

## Pseudocode

```
EVERY 30 SECONDS:
    for each order in QUEUE:
        waitScore = min(100, (waitMinutes / 10) × 100)
        complexityScore = ((8 - prepTime) / 6) × 100
        loyaltyScore = isRegular ? 50 + tier×10 : tier×10
        urgencyScore = calculateUrgency(waitMinutes)
        
        priority = 0.40×waitScore + 0.25×complexityScore 
                 + 0.10×loyaltyScore + 0.25×urgencyScore
        
        # Fairness adjustment
        if skipCount > 3:
            priority += (skipCount - 3) × 15
        
        # Emergency handling
        if waitMinutes >= 9:
            FORCE_ASSIGN(order)
            ALERT_MANAGER()
        elif waitMinutes >= 8:
            LOG_WARNING(order)

ON BARISTA FREE:
    order = QUEUE.pop_highest_priority()
    
    # Workload balancing
    if barista.workloadRatio > 1.2:
        if order.prepTime > 3:
            skip_to_next_barista()
    
    ASSIGN(order, barista)
    UPDATE_SKIP_COUNTS()
```

---

## Fairness Mechanism

| Skip Count | Action |
|------------|--------|
| 1-3 | Normal (no adjustment) |
| 4+ | +15 priority per skip beyond 3 |

**Transparency**: Each order includes `priorityExplanation` showing exact score breakdown.

---

## Workload Balancing

| Workload Ratio | Status | Rule |
|----------------|--------|------|
| > 1.2× avg | Overloaded | Only short orders (≤3 min) |
| 0.8-1.2× avg | Balanced | Normal assignment |
| < 0.8× avg | Underutilized | Priority for complex orders |

---

## Emergency Handling

| Wait Time | Action |
|-----------|--------|
| 8 min | WARNING alert, urgency = 75-100 |
| 9 min | FORCE assign to soonest barista |
| 9 min | Manager ALERT triggered |

---

## Example Calculation

**Order**: Latte (4 min prep), Gold Loyalty (Tier 4), Regular Customer, Waiting 5 min

```
WaitTimeScore    = (5/10) × 100 = 50
ComplexityScore  = ((8-4)/6) × 100 = 66.7
LoyaltyScore     = 50 + (4×10) = 90
UrgencyScore     = (5/6) × 25 = 20.8

Priority = (0.40 × 50) + (0.25 × 66.7) + (0.10 × 90) + (0.25 × 20.8)
         = 20 + 16.7 + 9 + 5.2
         = 50.9
```
