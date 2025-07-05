package com.gogidix.socialcommerce.adminfinalization.service;

import com.gogidix.socialcommerce.adminfinalization.dto.WorkflowRequest;
import com.gogidix.socialcommerce.adminfinalization.dto.WorkflowResponse;

import java.util.List;

public interface AdminWorkflowService {
    
    WorkflowResponse createWorkflow(WorkflowRequest request);
    
    WorkflowResponse getWorkflow(Long id);
    
    List<WorkflowResponse> getAllWorkflows(int page, int size);
    
    WorkflowResponse updateWorkflow(Long id, WorkflowRequest request);
    
    void deleteWorkflow(Long id);
    
    WorkflowResponse approveWorkflow(Long id);
    
    WorkflowResponse rejectWorkflow(Long id, String reason);
}