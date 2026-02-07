package com.coffeeshop.scheduler.controller;

import com.coffeeshop.scheduler.model.Barista;
import com.coffeeshop.scheduler.model.Order;
import com.coffeeshop.scheduler.service.SchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for the Coffee Shop Scheduler.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class SchedulerController {
    
    @Autowired
    private SchedulerService schedulerService;
    
    // ═══════════════════════════════════════════════════════════════
    // ORDER ENDPOINTS
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Create a new order with full priority parameters.
     * POST /api/orders
     * Body: { "drinkName": "Latte", "prepTimeMinutes": 4, "loyaltyTier": 3, "isRegularCustomer": true }
     */
    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        int loyaltyTier = request.loyaltyTier != null ? request.loyaltyTier : 1;
        boolean isRegular = request.isRegularCustomer != null ? request.isRegularCustomer : false;
        String username = request.username != null ? request.username : null;
        
        Order order = schedulerService.addOrder(
            request.drinkName, 
            request.prepTimeMinutes,
            loyaltyTier,
            isRegular,
            username
        );
        return ResponseEntity.ok(order);
    }
    
    /**
     * Get all orders in queue (sorted by priority).
     */
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getQueue(@RequestParam(required = false) String username) {
        return ResponseEntity.ok(schedulerService.getQueue(username));
    }
    
    /**
     * Get specific order status with priority explanation.
     */
    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable long id) {
        Order order = schedulerService.getOrder(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // BARISTA ENDPOINTS
    // ═══════════════════════════════════════════════════════════════
    
    @GetMapping("/baristas")
    public ResponseEntity<List<Barista>> getBaristas() {
        return ResponseEntity.ok(schedulerService.getBaristas());
    }
    
    @PostMapping("/baristas/{id}/complete")
    public ResponseEntity<Order> completeOrder(@PathVariable long id) {
        Order completed = schedulerService.completeOrder(id);
        if (completed == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(completed);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // STATS & ALERTS
    // ═══════════════════════════════════════════════════════════════
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(@RequestParam(required = false) String username) {
        return ResponseEntity.ok(schedulerService.getStats(username));
    }
    
    @GetMapping("/alerts")
    public ResponseEntity<List<String>> getAlerts() {
        return ResponseEntity.ok(schedulerService.getAlerts());
    }
    
    @PostMapping("/recalculate")
    public ResponseEntity<String> recalculate() {
        schedulerService.recalculatePriorities();
        return ResponseEntity.ok("Priorities recalculated");
    }
    
    /**
     * Get detailed barista statistics for each barista
     */
    @GetMapping("/stats/baristas")
    public ResponseEntity<List<Map<String, Object>>> getBaristaStats() {
        return ResponseEntity.ok(schedulerService.getBaristaStats());
    }
    
    /**
     * Run test simulation with 10 test cases
     * Each test case simulates 200-300 orders
     */
    @PostMapping("/simulation/run")
    public ResponseEntity<List<Map<String, Object>>> runSimulation(
            @RequestParam(defaultValue = "10") int testCases) {
        return ResponseEntity.ok(schedulerService.runTestSimulation(testCases));
    }
    
    // Request DTO with all priority parameters
    public static class OrderRequest {
        public String drinkName;
        public int prepTimeMinutes;
        public Integer loyaltyTier;          // 1-5, optional (default: 1)
        public Boolean isRegularCustomer;    // optional (default: false)
        public String username;              // User who created the order
    }
}
