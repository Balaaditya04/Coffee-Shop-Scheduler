package com.coffeeshop.scheduler.repository;

import com.coffeeshop.scheduler.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByBaristaName(String baristaName);
    List<Complaint> findAllByOrderByCreatedAtDesc();
}
