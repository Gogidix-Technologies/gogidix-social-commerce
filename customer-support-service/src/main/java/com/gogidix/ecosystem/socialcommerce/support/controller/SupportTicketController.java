package com.gogidix.ecosystem.socialcommerce.support.controller;

import com.gogidix.ecosystem.socialcommerce.support.dto.*;
import com.gogidix.ecosystem.socialcommerce.support.entity.SupportTicket;
import com.gogidix.ecosystem.socialcommerce.support.service.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Customer Support Ticket operations
 * Provides comprehensive support management aligned with UI/UX specifications
 */
@RestController
@RequestMapping("/api/v1/support/tickets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Support Tickets", description = "Customer Support Ticket Management API")
@CrossOrigin(origins = {"https://social.exalt.com", "https://admin.exalt.com", "https://dashboard.exalt.com"})
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    // ====================
    // TICKET CREATION
    // ====================

    @Operation(
        summary = "Create a new support ticket",
        description = "Creates a new customer support ticket with order tracking integration support"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ticket created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<SupportTicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request) {
        log.info("Creating new support ticket for customer: {}", request.getCustomerId());
        
        SupportTicketResponse response = supportTicketService.createTicket(request);
        
        log.info("Created support ticket: {} for customer: {}", 
                response.getTicketNumber(), request.getCustomerId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ====================
    // TICKET RETRIEVAL
    // ====================

    @Operation(summary = "Get ticket by ID")
    @GetMapping("/{ticketId}")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN') or @securityService.isTicketOwner(authentication.name, #ticketId)")
    public ResponseEntity<SupportTicketResponse> getTicketById(
            @PathVariable @Parameter(description = "Ticket ID") Long ticketId) {
        log.debug("Retrieving ticket by ID: {}", ticketId);
        
        SupportTicketResponse response = supportTicketService.getTicketById(ticketId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get ticket by ticket number")
    @GetMapping("/number/{ticketNumber}")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN') or @securityService.isTicketOwnerByNumber(authentication.name, #ticketNumber)")
    public ResponseEntity<SupportTicketResponse> getTicketByNumber(
            @PathVariable @Parameter(description = "Ticket number") String ticketNumber) {
        log.debug("Retrieving ticket by number: {}", ticketNumber);
        
        SupportTicketResponse response = supportTicketService.getTicketByNumber(ticketNumber);
        return ResponseEntity.ok(response);
    }

    // ====================
    // CUSTOMER PORTAL ENDPOINTS
    // ====================

    @Operation(
        summary = "Get customer's tickets",
        description = "Retrieves all tickets for a specific customer (Customer Portal)"
    )
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN') or @securityService.isCustomer(authentication.name, #customerId)")
    public ResponseEntity<Page<SupportTicketResponse>> getCustomerTickets(
            @PathVariable @Parameter(description = "Customer ID") Long customerId,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving tickets for customer: {}", customerId);
        
        Page<SupportTicketResponse> response = supportTicketService.getCustomerTickets(customerId, pageable);
        return ResponseEntity.ok(response);
    }

    // ====================
    // AGENT DASHBOARD ENDPOINTS
    // ====================

    @Operation(
        summary = "Get agent's assigned tickets",
        description = "Retrieves all tickets assigned to a specific agent (Agent Dashboard)"
    )
    @GetMapping("/agent/{agentId}")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN') or @securityService.isAgent(authentication.name, #agentId)")
    public ResponseEntity<Page<SupportTicketResponse>> getAgentTickets(
            @PathVariable @Parameter(description = "Agent ID") Long agentId,
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("Retrieving tickets for agent: {}", agentId);
        
        Page<SupportTicketResponse> response = supportTicketService.getAgentTickets(agentId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get team's assigned tickets",
        description = "Retrieves all tickets assigned to a specific team (Team Lead Dashboard)"
    )
    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasRole('TEAM_LEAD') or hasRole('ADMIN') or @securityService.isTeamMember(authentication.name, #teamId)")
    public ResponseEntity<Page<SupportTicketResponse>> getTeamTickets(
            @PathVariable @Parameter(description = "Team ID") Long teamId,
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("Retrieving tickets for team: {}", teamId);
        
        Page<SupportTicketResponse> response = supportTicketService.getTeamTickets(teamId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get unassigned tickets",
        description = "Retrieves all unassigned tickets for agent assignment (Agent Queue)"
    )
    @GetMapping("/unassigned")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<Page<SupportTicketResponse>> getUnassignedTickets(
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("Retrieving unassigned tickets");
        
        Page<SupportTicketResponse> response = supportTicketService.getUnassignedTickets(pageable);
        return ResponseEntity.ok(response);
    }

    // ====================
    // FILTERING ENDPOINTS
    // ====================

    @Operation(summary = "Get tickets by status")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<Page<SupportTicketResponse>> getTicketsByStatus(
            @PathVariable @Parameter(description = "Ticket status") SupportTicket.TicketStatus status,
            @PageableDefault(size = 50) Pageable pageable) {
        
        Page<SupportTicketResponse> response = supportTicketService.getTicketsByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get tickets by priority")
    @GetMapping("/priority/{priority}")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<Page<SupportTicketResponse>> getTicketsByPriority(
            @PathVariable @Parameter(description = "Ticket priority") SupportTicket.TicketPriority priority,
            @PageableDefault(size = 50) Pageable pageable) {
        
        Page<SupportTicketResponse> response = supportTicketService.getTicketsByPriority(priority, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get tickets by category")
    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<Page<SupportTicketResponse>> getTicketsByCategory(
            @PathVariable @Parameter(description = "Ticket category") SupportTicket.TicketCategory category,
            @PageableDefault(size = 50) Pageable pageable) {
        
        Page<SupportTicketResponse> response = supportTicketService.getTicketsByCategory(category, pageable);
        return ResponseEntity.ok(response);
    }

    // ====================
    // ORDER TRACKING INTEGRATION
    // ====================

    @Operation(
        summary = "Get tickets by order ID",
        description = "Retrieves tickets related to a specific order (Order Tracking Integration)"
    )
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN') or @securityService.canAccessOrder(authentication.name, #orderId)")
    public ResponseEntity<List<SupportTicketResponse>> getTicketsByOrderId(
            @PathVariable @Parameter(description = "Order ID") Long orderId) {
        log.debug("Retrieving tickets for order: {}", orderId);
        
        List<SupportTicketResponse> response = supportTicketService.getTicketsByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get tickets by tracking number",
        description = "Retrieves tickets related to a specific tracking number (Delivery Support)"
    )
    @GetMapping("/tracking/{trackingNumber}")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN') or @securityService.canAccessTracking(authentication.name, #trackingNumber)")
    public ResponseEntity<List<SupportTicketResponse>> getTicketsByTrackingNumber(
            @PathVariable @Parameter(description = "Tracking number") String trackingNumber) {
        log.debug("Retrieving tickets for tracking number: {}", trackingNumber);
        
        List<SupportTicketResponse> response = supportTicketService.getTicketsByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get delivery-related tickets",
        description = "Retrieves all tickets related to delivery issues (Delivery Support Dashboard)"
    )
    @GetMapping("/delivery-related")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<Page<SupportTicketResponse>> getDeliveryRelatedTickets(
            @PageableDefault(size = 50) Pageable pageable) {
        log.debug("Retrieving delivery-related tickets");
        
        Page<SupportTicketResponse> response = supportTicketService.getDeliveryRelatedTickets(pageable);
        return ResponseEntity.ok(response);
    }

    // ====================
    // TICKET MANAGEMENT
    // ====================

    @Operation(summary = "Update ticket status")
    @PutMapping("/{ticketId}/status")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<SupportTicketResponse> updateTicketStatus(
            @PathVariable Long ticketId,
            @RequestParam @Parameter(description = "New status") SupportTicket.TicketStatus status,
            @RequestParam(required = false) @Parameter(description = "Status change reason") String reason) {
        
        log.info("Updating ticket {} status to: {}", ticketId, status);
        
        SupportTicketResponse response = supportTicketService.updateTicketStatus(ticketId, status, reason);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update ticket priority")
    @PutMapping("/{ticketId}/priority")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<SupportTicketResponse> updateTicketPriority(
            @PathVariable Long ticketId,
            @RequestParam @Parameter(description = "New priority") SupportTicket.TicketPriority priority,
            @RequestParam(required = false) @Parameter(description = "Priority change reason") String reason) {
        
        log.info("Updating ticket {} priority to: {}", ticketId, priority);
        
        SupportTicketResponse response = supportTicketService.updateTicketPriority(ticketId, priority, reason);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Assign ticket to agent")
    @PutMapping("/{ticketId}/assign/agent")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<SupportTicketResponse> assignTicketToAgent(
            @PathVariable Long ticketId,
            @RequestParam @Parameter(description = "Agent ID") Long agentId,
            @RequestParam @Parameter(description = "Agent name") String agentName) {
        
        log.info("Assigning ticket {} to agent: {} ({})", ticketId, agentName, agentId);
        
        SupportTicketResponse response = supportTicketService.assignTicketToAgent(ticketId, agentId, agentName);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Assign ticket to team")
    @PutMapping("/{ticketId}/assign/team")
    @PreAuthorize("hasRole('TEAM_LEAD') or hasRole('ADMIN')")
    public ResponseEntity<SupportTicketResponse> assignTicketToTeam(
            @PathVariable Long ticketId,
            @RequestParam @Parameter(description = "Team ID") Long teamId,
            @RequestParam @Parameter(description = "Team name") String teamName) {
        
        log.info("Assigning ticket {} to team: {} ({})", ticketId, teamName, teamId);
        
        SupportTicketResponse response = supportTicketService.assignTicketToTeam(ticketId, teamId, teamName);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Escalate ticket")
    @PutMapping("/{ticketId}/escalate")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<SupportTicketResponse> escalateTicket(
            @PathVariable Long ticketId,
            @RequestParam @Parameter(description = "Escalation reason") String escalationReason) {
        
        log.info("Escalating ticket: {}", ticketId);
        
        SupportTicketResponse response = supportTicketService.escalateTicket(ticketId, escalationReason);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Close ticket with resolution")
    @PutMapping("/{ticketId}/close")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<SupportTicketResponse> closeTicket(
            @PathVariable Long ticketId,
            @RequestParam @Parameter(description = "Resolution notes") String resolutionNotes) {
        
        log.info("Closing ticket: {}", ticketId);
        
        SupportTicketResponse response = supportTicketService.closeTicket(ticketId, resolutionNotes);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reopen closed ticket")
    @PutMapping("/{ticketId}/reopen")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<SupportTicketResponse> reopenTicket(
            @PathVariable Long ticketId,
            @RequestParam @Parameter(description = "Reopen reason") String reason) {
        
        log.info("Reopening ticket: {}", ticketId);
        
        SupportTicketResponse response = supportTicketService.reopenTicket(ticketId, reason);
        return ResponseEntity.ok(response);
    }

    // ====================
    // MESSAGING
    // ====================

    @Operation(summary = "Add message to ticket")
    @PostMapping("/{ticketId}/messages")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN') or @securityService.isTicketOwner(authentication.name, #ticketId)")
    public ResponseEntity<TicketMessageResponse> addMessageToTicket(
            @PathVariable Long ticketId,
            @Valid @RequestBody AddMessageRequest request) {
        
        log.info("Adding message to ticket: {}", ticketId);
        
        TicketMessageResponse response = supportTicketService.addMessageToTicket(ticketId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get ticket messages")
    @GetMapping("/{ticketId}/messages")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN') or @securityService.isTicketOwner(authentication.name, #ticketId)")
    public ResponseEntity<Page<TicketMessageResponse>> getTicketMessages(
            @PathVariable Long ticketId,
            @PageableDefault(size = 50) Pageable pageable) {
        
        log.debug("Retrieving messages for ticket: {}", ticketId);
        
        Page<TicketMessageResponse> response = supportTicketService.getTicketMessages(ticketId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get full ticket conversation")
    @GetMapping("/{ticketId}/conversation")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN') or @securityService.isTicketOwner(authentication.name, #ticketId)")
    public ResponseEntity<List<TicketMessageResponse>> getTicketConversation(
            @PathVariable Long ticketId) {
        
        log.debug("Retrieving conversation for ticket: {}", ticketId);
        
        List<TicketMessageResponse> response = supportTicketService.getTicketConversation(ticketId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Mark messages as read")
    @PutMapping("/{ticketId}/messages/mark-read")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN') or @securityService.isTicketOwner(authentication.name, #ticketId)")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Long ticketId,
            @RequestParam Long userId) {
        
        log.debug("Marking messages as read for ticket: {} by user: {}", ticketId, userId);
        
        supportTicketService.markMessagesAsRead(ticketId, userId);
        return ResponseEntity.ok().build();
    }

    // ====================
    // AI-POWERED FEATURES
    // ====================

    @Operation(summary = "Generate AI response suggestion")
    @PostMapping("/{ticketId}/ai/suggest-response")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<AiSuggestedResponseDto> generateAiResponse(
            @PathVariable Long ticketId,
            @RequestParam String userMessage) {
        
        log.debug("Generating AI response for ticket: {}", ticketId);
        
        AiSuggestedResponseDto response = supportTicketService.generateAiResponse(ticketId, userMessage);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get suggested knowledge base articles")
    @GetMapping("/{ticketId}/ai/suggest-articles")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<List<KnowledgeBaseArticleDto>> suggestKnowledgeBaseArticles(
            @PathVariable Long ticketId) {
        
        log.debug("Getting suggested articles for ticket: {}", ticketId);
        
        List<KnowledgeBaseArticleDto> response = supportTicketService.suggestKnowledgeBaseArticles(ticketId);
        return ResponseEntity.ok(response);
    }

    // ====================
    // CUSTOMER SATISFACTION
    // ====================

    @Operation(summary = "Rate ticket satisfaction")
    @PostMapping("/{ticketId}/rating")
    @PreAuthorize("hasRole('CUSTOMER') or @securityService.isTicketOwner(authentication.name, #ticketId)")
    public ResponseEntity<SupportTicketResponse> rateTicket(
            @PathVariable Long ticketId,
            @RequestParam @Parameter(description = "Rating (1-5)") Integer rating,
            @RequestParam(required = false) @Parameter(description = "Feedback") String feedback) {
        
        log.info("Rating ticket {} with rating: {}", ticketId, rating);
        
        SupportTicketResponse response = supportTicketService.rateTicket(ticketId, rating, feedback);
        return ResponseEntity.ok(response);
    }

    // ====================
    // ANALYTICS & REPORTING
    // ====================

    @Operation(summary = "Get ticket statistics")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getTicketStatistics() {
        log.debug("Retrieving ticket statistics");
        
        Map<String, Object> statistics = supportTicketService.getTicketStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Get delivery-related statistics")
    @GetMapping("/statistics/delivery")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getDeliveryRelatedStats() {
        log.debug("Retrieving delivery-related statistics");
        
        Map<String, Object> statistics = supportTicketService.getDeliveryRelatedStats();
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Get AI performance statistics")
    @GetMapping("/statistics/ai")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getAiPerformanceStats() {
        log.debug("Retrieving AI performance statistics");
        
        Map<String, Object> statistics = supportTicketService.getAiPerformanceStats();
        return ResponseEntity.ok(statistics);
    }

    // ====================
    // SEARCH & FILTERING
    // ====================

    @Operation(summary = "Advanced ticket search")
    @PostMapping("/search")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<Page<SupportTicketResponse>> searchTickets(
            @Valid @RequestBody TicketSearchRequest searchRequest,
            @PageableDefault(size = 50) Pageable pageable) {
        
        log.debug("Performing advanced ticket search");
        
        Page<SupportTicketResponse> response = supportTicketService.searchTickets(searchRequest, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get overdue tickets")
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<List<SupportTicketResponse>> getOverdueTickets(
            @RequestParam(defaultValue = "24") @Parameter(description = "Overdue threshold in hours") int overdueHours) {
        
        log.debug("Retrieving overdue tickets (over {} hours)", overdueHours);
        
        List<SupportTicketResponse> response = supportTicketService.getOverdueTickets(overdueHours);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get SLA breached tickets")
    @GetMapping("/sla-breached")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<List<SupportTicketResponse>> getSlaBreachedTickets() {
        log.debug("Retrieving SLA breached tickets");
        
        List<SupportTicketResponse> response = supportTicketService.getSlaBreachedTickets();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get escalated tickets")
    @GetMapping("/escalated")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<SupportTicketResponse>> getEscalatedTickets() {
        log.debug("Retrieving escalated tickets");
        
        List<SupportTicketResponse> response = supportTicketService.getEscalatedTickets();
        return ResponseEntity.ok(response);
    }

    // ====================
    // BULK OPERATIONS
    // ====================

    @Operation(summary = "Bulk assign tickets to agent")
    @PutMapping("/bulk/assign/agent")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<BulkOperationResult> bulkAssignTickets(
            @RequestParam List<Long> ticketIds,
            @RequestParam Long agentId,
            @RequestParam String agentName) {
        
        log.info("Bulk assigning {} tickets to agent: {} ({})", ticketIds.size(), agentName, agentId);
        
        BulkOperationResult result = supportTicketService.bulkAssignTickets(ticketIds, agentId, agentName);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Bulk update ticket status")
    @PutMapping("/bulk/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<BulkOperationResult> bulkUpdateStatus(
            @RequestParam List<Long> ticketIds,
            @RequestParam SupportTicket.TicketStatus status) {
        
        log.info("Bulk updating status of {} tickets to: {}", ticketIds.size(), status);
        
        BulkOperationResult result = supportTicketService.bulkUpdateStatus(ticketIds, status);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Bulk update ticket priority")
    @PutMapping("/bulk/priority")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<BulkOperationResult> bulkUpdatePriority(
            @RequestParam List<Long> ticketIds,
            @RequestParam SupportTicket.TicketPriority priority) {
        
        log.info("Bulk updating priority of {} tickets to: {}", ticketIds.size(), priority);
        
        BulkOperationResult result = supportTicketService.bulkUpdatePriority(ticketIds, priority);
        return ResponseEntity.ok(result);
    }

    // ====================
    // EXPORT FUNCTIONALITY
    // ====================

    @Operation(summary = "Export tickets to Excel")
    @PostMapping("/export/excel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<byte[]> exportTicketsToExcel(
            @Valid @RequestBody TicketExportRequest request) {
        
        log.info("Exporting tickets to Excel format");
        
        byte[] excelData = supportTicketService.exportTicketsToExcel(request);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "support-tickets.xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    @Operation(summary = "Export tickets to PDF")
    @PostMapping("/export/pdf")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<byte[]> exportTicketsToPdf(
            @Valid @RequestBody TicketExportRequest request) {
        
        log.info("Exporting tickets to PDF format");
        
        byte[] pdfData = supportTicketService.exportTicketsToPdf(request);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "support-tickets.pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
    }

    // ====================
    // TAG MANAGEMENT
    // ====================

    @Operation(summary = "Add tag to ticket")
    @PostMapping("/{ticketId}/tags")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<SupportTicketResponse> addTagToTicket(
            @PathVariable Long ticketId,
            @RequestParam String tagName) {
        
        log.debug("Adding tag '{}' to ticket: {}", tagName, ticketId);
        
        SupportTicketResponse response = supportTicketService.addTagToTicket(ticketId, tagName);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove tag from ticket")
    @DeleteMapping("/{ticketId}/tags")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<SupportTicketResponse> removeTagFromTicket(
            @PathVariable Long ticketId,
            @RequestParam String tagName) {
        
        log.debug("Removing tag '{}' from ticket: {}", tagName, ticketId);
        
        SupportTicketResponse response = supportTicketService.removeTagFromTicket(ticketId, tagName);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get popular tags")
    @GetMapping("/tags/popular")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<List<TagDto>> getPopularTags() {
        log.debug("Retrieving popular tags");
        
        List<TagDto> tags = supportTicketService.getPopularTags();
        return ResponseEntity.ok(tags);
    }
}