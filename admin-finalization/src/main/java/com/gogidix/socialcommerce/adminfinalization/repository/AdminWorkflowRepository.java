package com.gogidix.socialcommerce.adminfinalization.repository;

import com.gogidix.socialcommerce.adminfinalization.model.AdminWorkflow;
import com.gogidix.socialcommerce.adminfinalization.model.WorkflowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminWorkflowRepository extends JpaRepository<AdminWorkflow, Long> {
    
    List<AdminWorkflow> findByStatus(WorkflowStatus status);
    
    List<AdminWorkflow> findByCreatedBy(String createdBy);
    
    List<AdminWorkflow> findByType(String type);
    
    @Query("SELECT w FROM AdminWorkflow w WHERE w.createdAt BETWEEN :startDate AND :endDate")
    List<AdminWorkflow> findWorkflowsInDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT w FROM AdminWorkflow w WHERE w.status = :status AND w.priority = :priority")
    List<AdminWorkflow> findByStatusAndPriority(WorkflowStatus status, String priority);
}