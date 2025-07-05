package com.gogidix.ecosystem.socialcommerce.support.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for ticket export requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketExportRequest {

    private TicketSearchRequest searchCriteria;
    private List<String> fieldsToInclude;
    private String format; // excel, pdf, csv
    private Boolean includeMessages;
    private Boolean includeAttachments;
    private Boolean includeActivities;
    private String fileName;
    private LocalDateTime exportedAt;
    private Long exportedBy;
}