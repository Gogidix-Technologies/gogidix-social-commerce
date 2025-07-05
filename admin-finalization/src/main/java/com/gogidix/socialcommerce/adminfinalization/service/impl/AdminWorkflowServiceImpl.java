package com.gogidix.socialcommerce.adminfinalization.service.impl;

import com.gogidix.socialcommerce.adminfinalization.dto.WorkflowRequest;
import com.gogidix.socialcommerce.adminfinalization.dto.WorkflowResponse;
import com.gogidix.socialcommerce.adminfinalization.exception.WorkflowNotFoundException;
import com.gogidix.socialcommerce.adminfinalization.model.AdminWorkflow;
import com.gogidix.socialcommerce.adminfinalization.model.WorkflowStatus;
import com.gogidix.socialcommerce.adminfinalization.repository.AdminWorkflowRepository;
import com.gogidix.socialcommerce.adminfinalization.service.AdminWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminWorkflowServiceImpl implements AdminWorkflowService {
    
    private final AdminWorkflowRepository workflowRepository;
    
    @Override
    public WorkflowResponse createWorkflow(WorkflowRequest request) {
        log.info("Creating new admin workflow: {}", request.getName());
        
        AdminWorkflow workflow = AdminWorkflow.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .priority(request.getPriority())
                .status(WorkflowStatus.PENDING)
                .createdBy(request.getCreatedBy())
                .createdAt(LocalDateTime.now())
                .build();
        
        workflow = workflowRepository.save(workflow);
        log.info("Created workflow with ID: {}", workflow.getId());
        
        return mapToResponse(workflow);
    }
    
    @Override
    public WorkflowResponse getWorkflow(Long id) {
        log.info("Fetching workflow with ID: {}", id);
        AdminWorkflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new WorkflowNotFoundException("Workflow not found with ID: " + id));
        return mapToResponse(workflow);
    }
    
    @Override
    public List<WorkflowResponse> getAllWorkflows(int page, int size) {
        log.info("Fetching all workflows - page: {}, size: {}", page, size);
        Page<AdminWorkflow> workflows = workflowRepository.findAll(PageRequest.of(page, size));
        return workflows.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public WorkflowResponse updateWorkflow(Long id, WorkflowRequest request) {
        log.info("Updating workflow with ID: {}", id);
        
        AdminWorkflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new WorkflowNotFoundException("Workflow not found with ID: " + id));
        
        workflow.setName(request.getName());
        workflow.setDescription(request.getDescription());
        workflow.setType(request.getType());
        workflow.setPriority(request.getPriority());
        workflow.setUpdatedAt(LocalDateTime.now());
        workflow.setUpdatedBy(request.getCreatedBy());
        
        workflow = workflowRepository.save(workflow);
        log.info("Updated workflow with ID: {}", workflow.getId());
        
        return mapToResponse(workflow);
    }
    
    @Override
    public void deleteWorkflow(Long id) {
        log.info("Deleting workflow with ID: {}", id);
        if (!workflowRepository.existsById(id)) {
            throw new WorkflowNotFoundException("Workflow not found with ID: " + id);
        }
        workflowRepository.deleteById(id);
        log.info("Deleted workflow with ID: {}", id);
    }
    
    @Override
    public WorkflowResponse approveWorkflow(Long id) {
        log.info("Approving workflow with ID: {}", id);
        
        AdminWorkflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new WorkflowNotFoundException("Workflow not found with ID: " + id));
        
        workflow.setStatus(WorkflowStatus.APPROVED);
        workflow.setApprovedAt(LocalDateTime.now());
        workflow.setUpdatedAt(LocalDateTime.now());
        
        workflow = workflowRepository.save(workflow);
        log.info("Approved workflow with ID: {}", workflow.getId());
        
        return mapToResponse(workflow);
    }
    
    @Override
    public WorkflowResponse rejectWorkflow(Long id, String reason) {
        log.info("Rejecting workflow with ID: {} for reason: {}", id, reason);
        
        AdminWorkflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new WorkflowNotFoundException("Workflow not found with ID: " + id));
        
        workflow.setStatus(WorkflowStatus.REJECTED);
        workflow.setRejectionReason(reason);
        workflow.setRejectedAt(LocalDateTime.now());
        workflow.setUpdatedAt(LocalDateTime.now());
        
        workflow = workflowRepository.save(workflow);
        log.info("Rejected workflow with ID: {}", workflow.getId());
        
        return mapToResponse(workflow);
    }
    
    private WorkflowResponse mapToResponse(AdminWorkflow workflow) {
        return WorkflowResponse.builder()
                .id(workflow.getId())
                .name(workflow.getName())
                .description(workflow.getDescription())
                .type(workflow.getType())
                .priority(workflow.getPriority())
                .status(workflow.getStatus().name())
                .createdBy(workflow.getCreatedBy())
                .createdAt(workflow.getCreatedAt())
                .updatedAt(workflow.getUpdatedAt())
                .approvedAt(workflow.getApprovedAt())
                .rejectedAt(workflow.getRejectedAt())
                .rejectionReason(workflow.getRejectionReason())
                .build();
    }
}