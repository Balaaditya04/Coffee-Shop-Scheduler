package com.coffeeshop.scheduler.model;

import java.time.Instant;

/**
 * Represents a coffee order in the system.
 * Includes all fields needed for the priority calculation formula.
 */
public class Order implements Comparable<Order> {
    
    private static long idCounter = 0;
    
    private final long id;
    private final Instant arrivalTime;
    private final String drinkName;
    private final int prepTimeMinutes;      // 2-8 minutes
    private final int loyaltyTier;          // 1 (new) to 5 (VIP)
    private final boolean isRegularCustomer;
    private String username;                 // User who created the order
    private double priority;                 // 0-100 score
    private int skipCount;                   // Fairness tracking
    private Long assignedBaristaId;
    private OrderStatus status;
    private String priorityExplanation;      // Transparency
    private Instant assignedTime;            // When order was assigned to barista
    private boolean autoComplaintRaised;     // Prevent duplicate auto-complaints
    
    public Order(String drinkName, int prepTimeMinutes, int loyaltyTier, boolean isRegularCustomer, String username) {
        this.id = ++idCounter;
        this.arrivalTime = Instant.now();
        this.drinkName = drinkName;
        this.prepTimeMinutes = Math.max(2, Math.min(8, prepTimeMinutes)); // Clamp 2-8
        this.loyaltyTier = Math.max(1, Math.min(5, loyaltyTier)); // Clamp 1-5
        this.isRegularCustomer = isRegularCustomer;
        this.username = username;
        this.priority = 0;
        this.skipCount = 0;
        this.assignedBaristaId = null;
        this.status = OrderStatus.QUEUED;
        this.priorityExplanation = "";
        this.assignedTime = null;
        this.autoComplaintRaised = false;
    }
    
    // Backwards-compatible constructor (no username)
    public Order(String drinkName, int prepTimeMinutes, int loyaltyTier, boolean isRegularCustomer) {
        this(drinkName, prepTimeMinutes, loyaltyTier, isRegularCustomer, null);
    }
    
    // Convenience constructor for simple orders
    public Order(String drinkName, int prepTimeMinutes) {
        this(drinkName, prepTimeMinutes, 1, false);
    }
    
    // Calculate wait time in seconds
    public double getWaitTimeSeconds() {
        return (Instant.now().toEpochMilli() - arrivalTime.toEpochMilli()) / 1000.0;
    }
    
    // Calculate wait time in minutes
    public double getWaitTimeMinutes() {
        return getWaitTimeSeconds() / 60.0;
    }
    
    // Check if order should be auto-completed
    public boolean isAutoCompleteReady() {
        if (assignedTime == null || status != OrderStatus.IN_PROGRESS) {
            return false;
        }
        long elapsedMs = Instant.now().toEpochMilli() - assignedTime.toEpochMilli();
        long prepTimeMs = prepTimeMinutes * 60 * 1000L;
        return elapsedMs >= prepTimeMs;
    }
    
    // Priority comparison (higher priority = comes first in max-heap)
    @Override
    public int compareTo(Order other) {
        return Double.compare(other.priority, this.priority);
    }
    
    // Getters
    public long getId() { return id; }
    public Instant getArrivalTime() { return arrivalTime; }
    public String getDrinkName() { return drinkName; }
    public int getPrepTimeMinutes() { return prepTimeMinutes; }
    public int getLoyaltyTier() { return loyaltyTier; }
    public boolean isRegularCustomer() { return isRegularCustomer; }
    public double getPriority() { return priority; }
    public int getSkipCount() { return skipCount; }
    public Long getAssignedBaristaId() { return assignedBaristaId; }
    public OrderStatus getStatus() { return status; }
    public String getPriorityExplanation() { return priorityExplanation; }
    public String getUsername() { return username; }
    public Instant getAssignedTime() { return assignedTime; }
    public boolean isAutoComplaintRaised() { return autoComplaintRaised; }
    
    // Setters
    public void setPriority(double priority) { this.priority = priority; }
    public void incrementSkipCount() { this.skipCount++; }
    public void setAssignedBaristaId(Long id) { this.assignedBaristaId = id; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setPriorityExplanation(String explanation) { this.priorityExplanation = explanation; }
    public void setUsername(String username) { this.username = username; }
    public void setAssignedTime(Instant assignedTime) { this.assignedTime = assignedTime; }
    public void setAutoComplaintRaised(boolean raised) { this.autoComplaintRaised = raised; }
    
    public enum OrderStatus {
        QUEUED, ASSIGNED, IN_PROGRESS, COMPLETED
    }
}

