package com.exalt.socialcommerce.adminfinalization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowRequest {
    
    @NotBlank(message = "Workflow name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotBlank(message = "Workflow type is required")
    private String type;
    
    @NotBlank(message = "Priority is required")
    private String priority;
    
    @NotBlank(message = "Creator is required")
    private String createdBy;
}