package com.gogidix.ecosystem.socialcommerce.support.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for bulk operation results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkOperationResult {

    private Integer totalRequested;
    private Integer successCount;
    private Integer failureCount;
    private List<Long> successfulTicketIds;
    private List<BulkOperationError> errors;
    private String operationType;
    private String operationDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkOperationError {
        private Long ticketId;
        private String errorMessage;
        private String errorCode;
    }
}