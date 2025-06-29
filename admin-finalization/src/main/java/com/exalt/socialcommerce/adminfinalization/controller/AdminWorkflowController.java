package com.exalt.socialcommerce.adminfinalization.controller;

import com.exalt.socialcommerce.adminfinalization.dto.WorkflowRequest;
import com.exalt.socialcommerce.adminfinalization.dto.WorkflowResponse;
import com.exalt.socialcommerce.adminfinalization.service.AdminWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin-finalization/workflows")
@RequiredArgsConstructor
public class AdminWorkflowController {
    
    private final AdminWorkflowService workflowService;
    
    @PostMapping
    public ResponseEntity<WorkflowResponse> createWorkflow(@Valid @RequestBody WorkflowRequest request) {
        WorkflowResponse response = workflowService.createWorkflow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<WorkflowResponse> getWorkflow(@PathVariable Long id) {
        WorkflowResponse response = workflowService.getWorkflow(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<WorkflowResponse>> getAllWorkflows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<WorkflowResponse> workflows = workflowService.getAllWorkflows(page, size);
        return ResponseEntity.ok(workflows);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<WorkflowResponse> updateWorkflow(
            @PathVariable Long id, 
            @Valid @RequestBody WorkflowRequest request) {
        WorkflowResponse response = workflowService.updateWorkflow(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable Long id) {
        workflowService.deleteWorkflow(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/approve")
    public ResponseEntity<WorkflowResponse> approveWorkflow(@PathVariable Long id) {
        WorkflowResponse response = workflowService.approveWorkflow(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/reject")
    public ResponseEntity<WorkflowResponse> rejectWorkflow(
            @PathVariable Long id,
            @RequestParam String reason) {
        WorkflowResponse response = workflowService.rejectWorkflow(id, reason);
        return ResponseEntity.ok(response);
    }
}