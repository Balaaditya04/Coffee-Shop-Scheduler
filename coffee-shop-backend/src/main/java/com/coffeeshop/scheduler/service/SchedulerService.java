package com.coffeeshop.scheduler.service;

import com.coffeeshop.scheduler.model.Barista;
import com.coffeeshop.scheduler.model.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.coffeeshop.scheduler.entity.Complaint;
import com.coffeeshop.scheduler.repository.ComplaintRepository;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Random;
import java.time.Instant;

/**
 * Main formula to understand
 * Priority = (0.40 × WaitTimeScore) + (0.25 × ComplexityScore) 
 *          + (0.10 × LoyaltyScore) + (0.25 × UrgencyScore)
 */
@Service
public class SchedulerService {
    
    // WEIGHT
    private static final double WEIGHT_WAIT_TIME = 0.40;    // 40%
    private static final double WEIGHT_COMPLEXITY = 0.25;   // 25%
    private static final double WEIGHT_LOYALTY = 0.10;      // 10%
    private static final double WEIGHT_URGENCY = 0.25;      // 25%
    
    // Thresholds(in Mins)
    private static final double MAX_WAIT_TIME_MINUTES = 10.0;
    private static final double EMERGENCY_THRESHOLD_MINUTES = 8.0;
    private static final double CRITICAL_THRESHOLD_MINUTES = 9.0;
    
    // Workload thresholds
    private static final double OVERLOADED_THRESHOLD = 1.2;
    private static final double UNDERUTILIZED_THRESHOLD = 0.8;
    
    // Fairness threshold
    private static final int FAIRNESS_SKIP_THRESHOLD = 3;
    private static final double FAIRNESS_PENALTY_BOOST = 15.0;
    
    // Main order queue (acts as priority queue via sorting)
    private final Queue<Order> orderQueue = new ConcurrentLinkedQueue<>();
    
    // Baristas
    private final List<Barista> baristas = new ArrayList<>();
    
    // Completed orders (for stats)
    private final List<Order> completedOrders = new ArrayList<>();
    
    // Alerts for manager
    private final List<String> alerts = new ArrayList<>();
    
    @Autowired
    private ComplaintRepository complaintRepository;
    
    @PostConstruct
    public void init() {
        baristas.add(new Barista(1, "Alice"));
        baristas.add(new Barista(2, "Bob"));
        baristas.add(new Barista(3, "Charlie"));
    }

    //PRIORITY CALCULATION



    private PriorityResult calculatePriority(Order order) {
        double waitMinutes = order.getWaitTimeMinutes();
        
        // 1. WAIT TIME SCORE (0-100)
        // Normalized based on 0-10 minute range
        double waitTimeScore = Math.min(100, (waitMinutes / MAX_WAIT_TIME_MINUTES) * 100);
        
        // 2. COMPLEXITY SCORE (0-100)

        double complexityScore = ((8.0 - order.getPrepTimeMinutes()) / 6.0) * 100;
        
        // 3. LOYALTY SCORE (0-100)


        double loyaltyScore;
        if (order.isRegularCustomer()) {
            // Regular customers: base 50 + tier bonus
            loyaltyScore = 50 + (order.getLoyaltyTier() * 10);
        } else {
            // New customers: just tier bonus (lower base)
            loyaltyScore = order.getLoyaltyTier() * 10;
        }
        loyaltyScore = Math.min(100, loyaltyScore);
        
        // 4. URGENCY SCORE (0-100)


        double urgencyScore;
        if (waitMinutes >= CRITICAL_THRESHOLD_MINUTES) {
            urgencyScore = 100;  // Maximum urgency
        } else if (waitMinutes >= EMERGENCY_THRESHOLD_MINUTES) {
            // Linear scale from 75-99 between 8-9 minutes
            urgencyScore = 75 + ((waitMinutes - 8) * 25);
        } else if (waitMinutes >= 6) {
            // Moderate urgency (6-8 minutes): 25-74
            urgencyScore = 25 + ((waitMinutes - 6) / 2 * 50);
        } else {
            // Low urgency (0-6 minutes): 0-24
            urgencyScore = (waitMinutes / 6) * 25;
        }
        
        // CALCULATE FINAL PRIORITY (0-100)
        double basePriority = 
            (WEIGHT_WAIT_TIME * waitTimeScore) +
            (WEIGHT_COMPLEXITY * complexityScore) +
            (WEIGHT_LOYALTY * loyaltyScore) +
            (WEIGHT_URGENCY * urgencyScore);
        
        // FAIRNESS ADJUSTMENT
        // If more than 3 later customers served first
        double fairnessBoost = 0;
        if (order.getSkipCount() > FAIRNESS_SKIP_THRESHOLD) {
            fairnessBoost = (order.getSkipCount() - FAIRNESS_SKIP_THRESHOLD) * FAIRNESS_PENALTY_BOOST;
        }
        
        double finalPriority = Math.min(100, basePriority + fairnessBoost);
        
        // Build explanation for transparency
        String explanation = String.format(
            "Wait: %.1f (×0.40=%.1f) + Complexity: %.1f (×0.25=%.1f) + " +
            "Loyalty: %.1f (×0.10=%.1f) + Urgency: %.1f (×0.25=%.1f)" +
            (fairnessBoost > 0 ? " + Fairness: +%.1f" : "") +
            " = %.1f",
            waitTimeScore, waitTimeScore * WEIGHT_WAIT_TIME,
            complexityScore, complexityScore * WEIGHT_COMPLEXITY,
            loyaltyScore, loyaltyScore * WEIGHT_LOYALTY,
            urgencyScore, urgencyScore * WEIGHT_URGENCY,
            fairnessBoost,
            finalPriority
        );
        
        return new PriorityResult(finalPriority, explanation);
    }
    
    private static class PriorityResult {
        final double priority;
        final String explanation;
        
        PriorityResult(double priority, String explanation) {
            this.priority = priority;
            this.explanation = explanation;
        }
    }
    

    // ORDER MANAGEMENT
    
    public Order addOrder(String drinkName, int prepTimeMinutes, int loyaltyTier, boolean isRegularCustomer, String username) {
        Order order = new Order(drinkName, prepTimeMinutes, loyaltyTier, isRegularCustomer, username);
        PriorityResult result = calculatePriority(order);
        order.setPriority(result.priority);
        order.setPriorityExplanation(result.explanation);
        orderQueue.add(order);
        
        tryAssignOrders();
        return order;
    }
    
    // Backwards-compatible overloads
    public Order addOrder(String drinkName, int prepTimeMinutes, int loyaltyTier, boolean isRegularCustomer) {
        return addOrder(drinkName, prepTimeMinutes, loyaltyTier, isRegularCustomer, null);
    }
    
    public Order addOrder(String drinkName, int prepTimeMinutes) {
        return addOrder(drinkName, prepTimeMinutes, 1, false, null);
    }
    
    public List<Order> getQueue() {
        return getQueue(null);
    }
    
    public List<Order> getQueue(String username) {
        List<Order> sorted = new ArrayList<>(orderQueue);
        if (username != null && !username.isEmpty()) {
            sorted.removeIf(o -> !username.equals(o.getUsername()));
        }
        sorted.sort(Comparator.comparingDouble(Order::getPriority).reversed());
        return sorted;
    }
    
    public Order getOrder(long orderId) {
        for (Order o : orderQueue) {
            if (o.getId() == orderId) return o;
        }
        for (Barista b : baristas) {
            for (Order o : b.getAssignedOrders()) {
                if (o.getId() == orderId) return o;
            }
        }
        return null;
    }

    // ASSIGNMENT LOGIC WITH WORKLOAD BALANCING

    
    public synchronized void tryAssignOrders() {
        List<Order> sorted = getQueue();
        
        for (Order order : sorted) {
            Barista available = getAvailableBarista(order);
            if (available != null) {
                orderQueue.remove(order);
                available.assignOrder(order);
                updateSkipCounts(order);
            }
        }
    }
    
    /**
     * Get available barista considering workload balance rules:
     * - If overloaded (> 1.2× average): prefer shorter orders
     * - If underutilized (< 0.8× average): allow complex orders
     */
    private Barista getAvailableBarista(Order order) {
        double avgWorkload = getAverageWorkloadMinutes();
        
        List<Barista> availableBaristas = new ArrayList<>();
        for (Barista b : baristas) {
            if (b.isAvailable()) {
                availableBaristas.add(b);
            }
        }
        
        if (availableBaristas.isEmpty()) {
            return null;
        }
        
        // Sort by workload (least loaded first)
        availableBaristas.sort(Comparator.comparingInt(Barista::getTotalPendingMinutes));
        
        for (Barista barista : availableBaristas) {
            double workloadRatio = avgWorkload > 0 ? 
                barista.getTotalPendingMinutes() / avgWorkload : 1.0;
            
            // Workload balancing rules
            if (workloadRatio > OVERLOADED_THRESHOLD) {
                // Overloaded: only accept short orders (≤ 3 min)
                if (order.getPrepTimeMinutes() <= 3) {
                    return barista;
                }
                // Skip this barista for complex orders
            } else if (workloadRatio < UNDERUTILIZED_THRESHOLD) {
                // Underutilized: accept any order (including complex)
                return barista;
            } else {
                // Normal workload: accept any order
                return barista;
            }
        }
        
        // Fallback: return least loaded if no ideal match
        return availableBaristas.get(0);
    }
    
    private double getAverageWorkloadMinutes() {
        int total = baristas.stream()
                .mapToInt(Barista::getTotalPendingMinutes)
                .sum();
        return (double) total / baristas.size();
    }
    
    // ════════════════════════════════════════════════════════════════
    // FAIRNESS TRACKING
    // ════════════════════════════════════════════════════════════════
    
    /**
     * Track when later arrivals are served before earlier ones
     */
    private void updateSkipCounts(Order assignedOrder) {
        for (Order order : orderQueue) {
            if (order.getArrivalTime().isBefore(assignedOrder.getArrivalTime())) {
                order.incrementSkipCount();
                
                // Alert if fairness threshold exceeded
                if (order.getSkipCount() == FAIRNESS_SKIP_THRESHOLD + 1) {
                    alerts.add(String.format(
                        "FAIRNESS: Order #%d has been skipped %d times. Priority boosted.",
                        order.getId(), order.getSkipCount()
                    ));
                }
            }
        }
    }
    

    // RECALCULATION (Every 30 seconds)

    
    @Scheduled(fixedRate = 30000)
    public void recalculatePriorities() {
        for (Order order : orderQueue) {
            PriorityResult result = calculatePriority(order);
            order.setPriority(result.priority);
            order.setPriorityExplanation(result.explanation);
            
            double waitMinutes = order.getWaitTimeMinutes();
            
            // EMERGENCY HANDLING
            if (waitMinutes >= CRITICAL_THRESHOLD_MINUTES) {
                forceAssign(order);
                alerts.add(String.format(
                    "CRITICAL: Order #%d (%.1f min wait) force-assigned! Manager alerted.",
                    order.getId(), waitMinutes
                ));
            } else if (waitMinutes >= EMERGENCY_THRESHOLD_MINUTES) {
                alerts.add(String.format(
                    "WARNING: Order #%d approaching timeout (%.1f min wait)",
                    order.getId(), waitMinutes
                ));
            }
        }
        
        tryAssignOrders();
    }
    

    // AUTO-COMPLETION TIMER (Every 1 second)

    
    /**
     * Auto-complete orders when prep time has elapsed.
     * Immediately assigns next order to free baristas.
     * Also handles auto-complaints for timeout orders.
     */
    @Scheduled(fixedRate = 1000)
    public synchronized void autoCompleteCheck() {
        // 1. Auto-complete orders that are done
        for (Barista barista : baristas) {
            Order current = barista.getCurrentOrder();
            
            if (current != null && current.isAutoCompleteReady()) {
                // Auto-complete the order
                Order completed = barista.completeCurrentOrder();
                if (completed != null) {
                    completedOrders.add(completed);
                }
                
                // Immediately try to assign next order to this now-free barista
                if (barista.isAvailable()) {
                    assignNextOrderToBarista(barista);
                }
            }
        }
        
        // 2. Handle timeout auto-complaints for orders waiting >= 10 minutes
        for (Order order : orderQueue) {
            if (order.getWaitTimeMinutes() >= MAX_WAIT_TIME_MINUTES && !order.isAutoComplaintRaised()) {
                raiseAutoComplaint(order);
            }
        }
        
        // 3. Ensure no barista is idle if there are orders
        for (Barista barista : baristas) {
            if (barista.isAvailable() && !orderQueue.isEmpty()) {
                assignNextOrderToBarista(barista);
            }
        }
    }
    
    /**
     * Assign the highest priority order to a specific barista
     */
    private void assignNextOrderToBarista(Barista barista) {
        if (!barista.isAvailable() || orderQueue.isEmpty()) {
            return;
        }
        
        // Get highest priority order
        List<Order> sorted = getQueue();
        if (!sorted.isEmpty()) {
            Order nextOrder = sorted.get(0);
            orderQueue.remove(nextOrder);
            barista.assignOrder(nextOrder);
            updateSkipCounts(nextOrder);
        }
    }
    
    /**
     * Auto-raise complaint for timeout (wait >= 10 min)
     */
    private void raiseAutoComplaint(Order order) {
        order.setAutoComplaintRaised(true);
        
        // Find which barista should have handled faster or is responsible
        String baristaName = "Unassigned";
        if (order.getAssignedBaristaId() != null) {
            Barista b = getBarista(order.getAssignedBaristaId());
            if (b != null) baristaName = b.getName();
        } else {
            // Pick a barista to "blame" or just mark as System
            baristaName = "System (Auto-Raised)";
        }
        
        // Create and save complaint
        Complaint complaint = new Complaint(
            baristaName,
            order.getUsername() != null ? order.getUsername() : "anonymous",
            "Auto-Raised (Timeout): Order #" + order.getId() + " (" + order.getDrinkName() + ") waited " + 
            Math.round(order.getWaitTimeMinutes() * 10) / 10.0 + " minutes."
        );
        
        if (complaintRepository != null) {
            complaintRepository.save(complaint);
        }
        
        // Log the auto-complaint
        alerts.add(String.format(
            "AUTO-COMPLAINT: Order #%d exceeded 10 min wait (%.1f min). Complaint filed against %s.",
            order.getId(), order.getWaitTimeMinutes(), baristaName
        ));
    }
    

    /**
     * Force assign critical order to next available barista
     */
    private void forceAssign(Order order) {
        Barista soonest = baristas.stream()
                .min(Comparator.comparingLong(Barista::getBusyUntilMs))
                .orElse(baristas.get(0));
        
        orderQueue.remove(order);
        soonest.assignOrder(order);
    }
    

    // BARISTA OPERATIONS

    
    public List<Barista> getBaristas() {
        return baristas;
    }
    
    public Barista getBarista(long id) {
        return baristas.stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElse(null);
    }
    
    public Order completeOrder(long baristaId) {
        Barista barista = getBarista(baristaId);
        if (barista != null) {
            Order completed = barista.completeCurrentOrder();
            if (completed != null) {
                completedOrders.add(completed);
                tryAssignOrders();
            }
            return completed;
        }
        return null;
    }
    

    // STATISTICS

    
    public Map<String, Object> getStats() {
        return getStats(null);
    }
    
    public Map<String, Object> getStats(String username) {
        Map<String, Object> stats = new HashMap<>();
        
        // Filter orders by username if provided
        List<Order> userQueue = new ArrayList<>(orderQueue);
        List<Order> userCompleted = new ArrayList<>(completedOrders);
        
        if (username != null && !username.isEmpty()) {
            userQueue.removeIf(o -> !username.equals(o.getUsername()));
            userCompleted.removeIf(o -> !username.equals(o.getUsername()));
        }
        
        stats.put("queueSize", userQueue.size());
        stats.put("completedCount", userCompleted.size());
        
        double avgWait = userCompleted.stream()
                .mapToDouble(Order::getWaitTimeMinutes)
                .average()
                .orElse(0);
        stats.put("averageWaitMinutes", Math.round(avgWait * 10) / 10.0);
        
        long timeouts = userCompleted.stream()
                .filter(o -> o.getWaitTimeMinutes() > 10)
                .count();
        stats.put("timeoutCount", timeouts);
        
        // Barista workloads with ratio (global - not user-specific)
        Map<String, Object> workloads = new HashMap<>();
        double avgWorkload = getAverageWorkloadMinutes();
        for (Barista b : baristas) {
            Map<String, Object> bData = new HashMap<>();
            bData.put("minutes", b.getTotalPendingMinutes());
            bData.put("ratio", avgWorkload > 0 ? 
                Math.round((b.getTotalPendingMinutes() / avgWorkload) * 100) / 100.0 : 1.0);
            workloads.put(b.getName(), bData);
        }
        stats.put("baristaWorkloads", workloads);
        
        return stats;
    }
    
    public List<String> getAlerts() {
        return new ArrayList<>(alerts);
    }
    
    public void clearAlerts() {
        alerts.clear();
    }
    

    // BARISTA STATISTICS

    
    /**
     * Get detailed statistics for each barista
     */
    public List<Map<String, Object>> getBaristaStats() {
        List<Map<String, Object>> baristaStats = new ArrayList<>();
        double avgWorkload = getAverageWorkloadMinutes();
        
        for (Barista b : baristas) {
            Map<String, Object> stats = new HashMap<>();
            stats.put("id", b.getId());
            stats.put("name", b.getName());
            stats.put("ordersCompleted", b.getOrdersCompleted());
            stats.put("totalWorkloadMinutes", b.getTotalPendingMinutes());
            
            // Calculate average time per order
            double avgTimePerOrder = b.getOrdersCompleted() > 0 ? 
                b.getTotalWorkloadMinutes() / (double) b.getOrdersCompleted() : 0;
            stats.put("avgTimePerOrder", Math.round(avgTimePerOrder * 10) / 10.0);
            
            // Workload balance ratio
            double ratio = avgWorkload > 0 ? b.getTotalPendingMinutes() / avgWorkload : 1.0;
            stats.put("workloadRatio", Math.round(ratio * 100) / 100.0);
            
            // Timeouts while assigned (orders that waited > 10 min)
            long timeouts = completedOrders.stream()
                .filter(o -> b.getId() == (o.getAssignedBaristaId() != null ? o.getAssignedBaristaId() : -1))
                .filter(o -> o.getWaitTimeMinutes() > 10)
                .count();
            stats.put("timeouts", timeouts);
            
            stats.put("available", b.isAvailable());
            baristaStats.add(stats);
        }
        
        return baristaStats;
    }
    
    // ════════════════════════════════════════════════════════════════
    // TEST SIMULATION
    // ════════════════════════════════════════════════════════════════
    
    public List<Map<String, Object>> runTestSimulation(int numTestCases) {
        List<Map<String, Object>> results = new ArrayList<>();
        Random random = new Random();
        
        // Menu based on problem statement
        String[] drinkTypes = {"Cold Brew", "Espresso", "Americano", "Cappuccino", "Latte", "Mocha"};
        int[] prepTimes = {1, 2, 2, 4, 4, 6};
        double[] frequencies = {0.25, 0.45, 0.60, 0.80, 0.92, 1.00}; 
        
        for (int testCase = 1; testCase <= numTestCases; testCase++) {
            List<SimOrder> pendingArrivals = new ArrayList<>();
            List<SimOrder> simQueue = new ArrayList<>();
            List<SimOrder> simCompleted = new ArrayList<>();
            int[] baristaOrders = new int[3]; 
            long[] baristaFreeAt = new long[3];
            double totalWaitTime = 0;
            int timeouts = 0;
            int abandoned = 0;
            
            // 1. Generate arrivals for 180 min (λ = 1.4/min)
            double lambdaPerMin = 1.4;
            long currentTimeMs = 0;
            int orderId = 1;
            while (currentTimeMs < 180 * 60 * 1000) {
                double interArrival = -Math.log(1.0 - random.nextDouble()) / lambdaPerMin;
                currentTimeMs += (long) (interArrival * 60 * 1000);
                if (currentTimeMs >= 180 * 60 * 1000) break;
                
                SimOrder o = new SimOrder();
                o.id = orderId++;
                double r = random.nextDouble();
                for (int i = 0; i < frequencies.length; i++) {
                    if (r <= frequencies[i]) {
                        o.drinkName = drinkTypes[i];
                        o.prepTime = prepTimes[i];
                        break;
                    }
                }
                o.loyaltyTier = 1 + random.nextInt(5);
                o.isRegular = random.nextDouble() < 0.4;
                o.arrivalTime = currentTimeMs;
                pendingArrivals.add(o);
            }
            
            // 2. Event Simulation
            long simTime = 0;
            long lastPriorityUpdate = -30000;
            
            while (!pendingArrivals.isEmpty() || !simQueue.isEmpty() || isAnyBaristaWorking(baristaFreeAt, simTime)) {
                
                // Add new arrivals
                while (!pendingArrivals.isEmpty() && pendingArrivals.get(0).arrivalTime <= simTime) {
                    simQueue.add(pendingArrivals.remove(0));
                }
                
                // Abandonment (New customers leave after 8 min)
                Iterator<SimOrder> qIt = simQueue.iterator();
                while (qIt.hasNext()) {
                    SimOrder o = qIt.next();
                    if (!o.isRegular && (simTime - o.arrivalTime >= 8 * 60 * 1000)) {
                        totalWaitTime += 8.0; // Wait was 8 mins
                        abandoned++;
                        simCompleted.add(o);
                        qIt.remove();
                    }
                }
                
                // Priority Update
                if (simTime - lastPriorityUpdate >= 30000) {
                    recalculateSimPriorities(simQueue, simTime);
                    lastPriorityUpdate = simTime;
                }
                
                // Timeout Assignment (10 minute rule)
                List<SimOrder> forceAssigns = new ArrayList<>();
                qIt = simQueue.iterator();
                while (qIt.hasNext()) {
                    SimOrder o = qIt.next();
                    if (simTime - o.arrivalTime >= 10 * 60 * 1000) {
                        forceAssigns.add(o);
                        qIt.remove();
                    }
                }
                
                // Process force assigns
                for (SimOrder o : forceAssigns) {
                    int b = 0;
                    for (int i = 1; i < 3; i++) if (baristaFreeAt[i] < baristaFreeAt[b]) b = i;
                    
                    long startAt = Math.max(simTime, baristaFreeAt[b]);
                    totalWaitTime += (startAt - o.arrivalTime) / 60000.0;
                    timeouts++;
                    baristaFreeAt[b] = startAt + (o.prepTime * 60000L);
                    baristaOrders[b]++;
                    simCompleted.add(o);
                }
                
                // Regular Assignment
                for (int b = 0; b < 3 && !simQueue.isEmpty(); b++) {
                    if (baristaFreeAt[b] <= simTime) {
                        SimOrder o = simQueue.remove(0);
                        totalWaitTime += (simTime - o.arrivalTime) / 60000.0;
                        baristaFreeAt[b] = simTime + (o.prepTime * 60000L);
                        baristaOrders[b]++;
                        simCompleted.add(o);
                    }
                }
                
                // Jump to next event
                long nextEvent = simTime + 1000;
                if (!pendingArrivals.isEmpty()) nextEvent = Math.min(nextEvent, pendingArrivals.get(0).arrivalTime);
                for (int b = 0; b < 3; b++) if (baristaFreeAt[b] > simTime) nextEvent = Math.min(nextEvent, baristaFreeAt[b]);
                
                simTime = nextEvent;
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("testCase", testCase);
            result.put("totalOrders", simCompleted.size());
            result.put("avgWaitTime", simCompleted.isEmpty() ? 0 : Math.round(totalWaitTime / simCompleted.size() * 10) / 10.0);
            result.put("timeouts", timeouts);
            result.put("abandoned", abandoned); // Added for clarity
            result.put("b1Orders", baristaOrders[0]);
            result.put("b2Orders", baristaOrders[1]);
            result.put("b3Orders", baristaOrders[2]);
            results.add(result);
        }
        return results;
    }
    
    private boolean isAnyBaristaWorking(long[] baristaFreeAt, long currentTime) {
        for (long f : baristaFreeAt) if (f > currentTime) return true;
        return false;
    }
    
    private void recalculateSimPriorities(List<SimOrder> queue, long currentTime) {
        for (SimOrder o : queue) {
            double wait = (currentTime - o.arrivalTime) / 60000.0;
            double waitScore = Math.min(100, (wait / 10.0) * 100);
            double compScore = ((8.0 - o.prepTime) / 6.0) * 100;
            double loyScore = o.isRegular ? 50 + o.loyaltyTier*10 : o.loyaltyTier*10;
            double urgScore = wait >= 9 ? 100 : (wait >= 8 ? 75 : (wait >= 6 ? 50 : (wait/6.0)*25));
            o.priority = (0.40 * waitScore) + (0.25 * compScore) + (0.10 * loyScore) + (0.25 * urgScore);
        }
        queue.sort((a,b) -> Double.compare(b.priority, a.priority));
    }
    
    private static class SimOrder {
        int id;
        String drinkName;
        int prepTime;
        int loyaltyTier;
        boolean isRegular;
        long arrivalTime;
        double priority;
    }
}
