package com.coffeeshop.scheduler.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a barista in the coffee shop.
 */
public class Barista {
    
    private final long id;
    private final String name;
    private final List<Order> assignedOrders;
    private boolean available;
    private long busyUntilMs;  // Timestamp when barista will be free
    private int ordersCompleted;     // Track total completed orders
    private int totalWorkloadMinutes; // Track total workload handled
    
    public Barista(long id, String name) {
        this.id = id;
        this.name = name;
        this.assignedOrders = new ArrayList<>();
        this.available = true;
        this.busyUntilMs = 0;
        this.ordersCompleted = 0;
        this.totalWorkloadMinutes = 0;
    }
    
    // Get total pending work time in minutes
    public int getTotalPendingMinutes() {
        return assignedOrders.stream()
                .filter(o -> o.getStatus() != Order.OrderStatus.COMPLETED)
                .mapToInt(Order::getPrepTimeMinutes)
                .sum();
    }
    
    // Assign an order to this barista
    public void assignOrder(Order order) {
        order.setAssignedBaristaId(this.id);
        order.setStatus(Order.OrderStatus.ASSIGNED);
        assignedOrders.add(order);
        
        if (available) {
            startNextOrder();
        }
    }
    
    // Start working on next order
    public void startNextOrder() {
        Order next = assignedOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.ASSIGNED)
                .findFirst()
                .orElse(null);
        
        if (next != null) {
            next.setStatus(Order.OrderStatus.IN_PROGRESS);
            next.setAssignedTime(Instant.now());  // Track when prep started
            this.available = false;
            this.busyUntilMs = System.currentTimeMillis() + (next.getPrepTimeMinutes() * 60000L);
        } else {
            this.available = true;
        }
    }
    
    // Get current in-progress order
    public Order getCurrentOrder() {
        return assignedOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.IN_PROGRESS)
                .findFirst()
                .orElse(null);
    }
    
    // Complete current order
    public Order completeCurrentOrder() {
        Order current = getCurrentOrder();
        
        if (current != null) {
            current.setStatus(Order.OrderStatus.COMPLETED);
            this.ordersCompleted++;
            this.totalWorkloadMinutes += current.getPrepTimeMinutes();
        }
        
        this.available = true;
        startNextOrder();  // Start next if available
        
        return current;
    }
    
    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public List<Order> getAssignedOrders() { return assignedOrders; }
    public boolean isAvailable() { return available; }
    public long getBusyUntilMs() { return busyUntilMs; }
    public int getOrdersCompleted() { return ordersCompleted; }
    public int getTotalWorkloadMinutes() { return totalWorkloadMinutes; }
    
    public void setAvailable(boolean available) { this.available = available; }
}

