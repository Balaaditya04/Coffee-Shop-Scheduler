package com.coffeeshop.scheduler.controller;

import com.coffeeshop.scheduler.entity.Complaint;
import com.coffeeshop.scheduler.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "http://localhost:3000")
public class ComplaintController {
    
    @Autowired
    private ComplaintRepository complaintRepository;
    
    /**
     * Submit a new complaint
     * POST /api/complaints
     */
    @PostMapping
    public ResponseEntity<Complaint> submitComplaint(@RequestBody ComplaintRequest request) {
        Complaint complaint = new Complaint(
            request.baristaName,
            request.username,
            request.message
        );
        Complaint saved = complaintRepository.save(complaint);
        return ResponseEntity.ok(saved);
    }
    
    /**
     * Get all complaints (newest first)
     * GET /api/complaints
     */
    @GetMapping
    public ResponseEntity<List<Complaint>> getAllComplaints() {
        return ResponseEntity.ok(complaintRepository.findAllByOrderByCreatedAtDesc());
    }
    
    /**
     * Get complaints for specific barista
     * GET /api/complaints/barista/{name}
     */
    @GetMapping("/barista/{name}")
    public ResponseEntity<List<Complaint>> getComplaintsByBarista(@PathVariable String name) {
        return ResponseEntity.ok(complaintRepository.findByBaristaName(name));
    }
    
    public static class ComplaintRequest {
        public String baristaName;
        public String username;
        public String message;
    }
}
