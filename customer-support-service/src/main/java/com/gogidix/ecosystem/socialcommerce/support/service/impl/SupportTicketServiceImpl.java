package com.gogidix.ecosystem.socialcommerce.support.service.impl;

import com.gogidix.ecosystem.socialcommerce.support.dto.*;
import com.gogidix.ecosystem.socialcommerce.support.entity.SupportTicket;
import com.gogidix.ecosystem.socialcommerce.support.entity.TicketMessage;
import com.gogidix.ecosystem.socialcommerce.support.repository.SupportTicketRepository;
import com.gogidix.ecosystem.socialcommerce.support.repository.TicketMessageRepository;
import com.gogidix.ecosystem.socialcommerce.support.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of SupportTicketService providing comprehensive customer support functionality
 * Aligned with UI/UX specifications and industrial best practices
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public SupportTicketResponse createTicket(CreateTicketRequest request) {
        log.info("Creating new support ticket for customer: {}", request.getCustomerId());
        
        SupportTicket ticket = SupportTicket.builder()
                .ticketNumber(generateTicketNumber())
                .customerId(request.getCustomerId())
                .customerEmail(request.getCustomerEmail())
                .customerName(request.getCustomerName())
                .subject(request.getSubject())
                .description(request.getDescription())
                .category(request.getCategory())
                .priority(determinePriority(request))
                .status(SupportTicket.TicketStatus.OPEN)
                .channel(request.getChannel())
                .orderId(request.getOrderId())
                .orderNumber(request.getOrderNumber())
                .trackingNumber(request.getTrackingNumber())
                .deliveryIssueType(request.getDeliveryIssueType())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .slaDueDate(calculateSlaTargetTime(request.getCategory(), request.getPriority()))
                .aiHandled(shouldHandleWithAi(request))
                .tags(new HashSet<>())
                .build();

        ticket = ticketRepository.save(ticket);
        
        // Create initial message with customer request
        createInitialMessage(ticket, request.getDescription(), request.getCustomerId());
        
        // Publish ticket creation event
        publishTicketCreatedEvent(ticket);
        
        // Attempt AI auto-resolution if eligible
        if (ticket.getAiHandled()) {
            attemptAiAutoResolution(ticket);
        }

        log.info("Created support ticket: {} for customer: {}", ticket.getTicketNumber(), request.getCustomerId());
        return mapToResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketResponse getTicketById(Long ticketId) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));
        return mapToResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketResponse getTicketByNumber(String ticketNumber) {
        SupportTicket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketNumber));
        return mapToResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getCustomerTickets(Long customerId, Pageable pageable) {
        Page<SupportTicket> tickets = ticketRepository.findByCustomerId(customerId, pageable);
        return tickets.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getAgentTickets(Long agentId, Pageable pageable) {
        Page<SupportTicket> tickets = ticketRepository.findByAssignedToAgentId(agentId, pageable);
        return tickets.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getTeamTickets(Long teamId, Pageable pageable) {
        Page<SupportTicket> tickets = ticketRepository.findByAssignedToTeamId(teamId, pageable);
        return tickets.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getUnassignedTickets(Pageable pageable) {
        Page<SupportTicket> tickets = ticketRepository.findByAssignedToAgentIdIsNull(pageable);
        return tickets.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getTicketsByStatus(SupportTicket.TicketStatus status, Pageable pageable) {
        Page<SupportTicket> tickets = ticketRepository.findByStatus(status, pageable);
        return tickets.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getTicketsByPriority(SupportTicket.TicketPriority priority, Pageable pageable) {
        Page<SupportTicket> tickets = ticketRepository.findByPriority(priority, pageable);
        return tickets.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getTicketsByCategory(SupportTicket.TicketCategory category, Pageable pageable) {
        Page<SupportTicket> tickets = ticketRepository.findByCategory(category, pageable);
        return tickets.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getTicketsByOrderId(Long orderId) {
        List<SupportTicket> tickets = ticketRepository.findByOrderId(orderId);
        return tickets.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getTicketsByTrackingNumber(String trackingNumber) {
        List<SupportTicket> tickets = ticketRepository.findByTrackingNumber(trackingNumber);
        return tickets.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getDeliveryRelatedTickets(Pageable pageable) {
        Page<SupportTicket> tickets = ticketRepository.findDeliveryRelatedTickets(pageable);
        return tickets.map(this::mapToResponse);
    }

    @Override
    public SupportTicketResponse updateTicketStatus(Long ticketId, SupportTicket.TicketStatus status, String reason) {
        SupportTicket ticket = getTicketEntityById(ticketId);
        SupportTicket.TicketStatus previousStatus = ticket.getStatus();
        
        ticket.setStatus(status);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        if (status == SupportTicket.TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
        }
        
        ticket = ticketRepository.save(ticket);
        
        // Log status change activity
        logTicketActivity(ticket, "STATUS_CHANGE", 
                String.format("Status changed from %s to %s. Reason: %s", previousStatus, status, reason));
        
        publishTicketStatusChangedEvent(ticket, previousStatus, reason);
        
        log.info("Updated ticket {} status from {} to {}", ticketId, previousStatus, status);
        return mapToResponse(ticket);
    }

    @Override
    public SupportTicketResponse updateTicketPriority(Long ticketId, SupportTicket.TicketPriority priority, String reason) {
        SupportTicket ticket = getTicketEntityById(ticketId);
        SupportTicket.TicketPriority previousPriority = ticket.getPriority();
        
        ticket.setPriority(priority);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticket.setSlaDueDate(calculateSlaTargetTime(ticket.getCategory(), priority));
        
        ticket = ticketRepository.save(ticket);
        
        logTicketActivity(ticket, "PRIORITY_CHANGE", 
                String.format("Priority changed from %s to %s. Reason: %s", previousPriority, priority, reason));
        
        log.info("Updated ticket {} priority from {} to {}", ticketId, previousPriority, priority);
        return mapToResponse(ticket);
    }

    @Override
    public SupportTicketResponse assignTicketToAgent(Long ticketId, Long agentId, String agentName) {
        SupportTicket ticket = getTicketEntityById(ticketId);
        
        ticket.setAssignedToAgentId(agentId);
        ticket.setAssignedToAgentName(agentName);
        ticket.setAssignedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        
        if (ticket.getStatus() == SupportTicket.TicketStatus.OPEN) {
            ticket.setStatus(SupportTicket.TicketStatus.IN_PROGRESS);
        }
        
        ticket = ticketRepository.save(ticket);
        
        logTicketActivity(ticket, "AGENT_ASSIGNMENT", 
                String.format("Ticket assigned to agent: %s (%d)", agentName, agentId));
        
        publishTicketAssignedEvent(ticket);
        
        log.info("Assigned ticket {} to agent: {} ({})", ticketId, agentName, agentId);
        return mapToResponse(ticket);
    }

    @Override
    public SupportTicketResponse assignTicketToTeam(Long ticketId, Long teamId, String teamName) {
        SupportTicket ticket = getTicketEntityById(ticketId);
        
        ticket.setAssignedToTeamId(teamId);
        ticket.setAssignedToTeamName(teamName);
        ticket.setAssignedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        
        ticket = ticketRepository.save(ticket);
        
        logTicketActivity(ticket, "TEAM_ASSIGNMENT", 
                String.format("Ticket assigned to team: %s (%d)", teamName, teamId));
        
        log.info("Assigned ticket {} to team: {} ({})", ticketId, teamName, teamId);
        return mapToResponse(ticket);
    }

    @Override
    public SupportTicketResponse escalateTicket(Long ticketId, String escalationReason) {
        SupportTicket ticket = getTicketEntityById(ticketId);
        
        ticket.setEscalated(true);
        ticket.setEscalatedAt(LocalDateTime.now());
        ticket.setEscalationReason(escalationReason);
        ticket.setPriority(escalatePriority(ticket.getPriority()));
        ticket.setUpdatedAt(LocalDateTime.now());
        
        ticket = ticketRepository.save(ticket);
        
        logTicketActivity(ticket, "ESCALATION", 
                String.format("Ticket escalated. Reason: %s", escalationReason));
        
        publishTicketEscalatedEvent(ticket);
        
        log.info("Escalated ticket {} with reason: {}", ticketId, escalationReason);
        return mapToResponse(ticket);
    }

    @Override
    public SupportTicketResponse closeTicket(Long ticketId, String resolutionNotes) {
        SupportTicket ticket = getTicketEntityById(ticketId);
        
        ticket.setStatus(SupportTicket.TicketStatus.CLOSED);
        ticket.setClosedAt(LocalDateTime.now());
        ticket.setResolutionNotes(resolutionNotes);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        ticket = ticketRepository.save(ticket);
        
        logTicketActivity(ticket, "CLOSURE", 
                String.format("Ticket closed with resolution: %s", resolutionNotes));
        
        publishTicketClosedEvent(ticket);
        
        log.info("Closed ticket {} with resolution notes", ticketId);
        return mapToResponse(ticket);
    }

    @Override
    public SupportTicketResponse reopenTicket(Long ticketId, String reason) {
        SupportTicket ticket = getTicketEntityById(ticketId);
        
        ticket.setStatus(SupportTicket.TicketStatus.REOPENED);
        ticket.setClosedAt(null);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        ticket = ticketRepository.save(ticket);
        
        logTicketActivity(ticket, "REOPENED", 
                String.format("Ticket reopened. Reason: %s", reason));
        
        log.info("Reopened ticket {} with reason: {}", ticketId, reason);
        return mapToResponse(ticket);
    }

    @Override
    public TicketMessageResponse addMessageToTicket(Long ticketId, AddMessageRequest request) {
        SupportTicket ticket = getTicketEntityById(ticketId);
        
        TicketMessage message = TicketMessage.builder()
                .ticket(ticket)
                .senderId(request.getSenderId())
                .senderName(request.getSenderName())
                .senderType(request.getSenderType())
                .content(request.getContent())
                .isInternal(request.getIsInternal())
                .createdAt(LocalDateTime.now())
                .build();
        
        message = messageRepository.save(message);
        
        // Update ticket last activity
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        
        // Publish message created event for real-time notifications
        publishMessageCreatedEvent(ticket, message);
        
        log.info("Added message to ticket {} by user: {}", ticketId, request.getSenderName());
        return mapToMessageResponse(message);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketMessageResponse> getTicketMessages(Long ticketId, Pageable pageable) {
        Page<TicketMessage> messages = messageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId, pageable);
        return messages.map(this::mapToMessageResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketMessageResponse> getTicketConversation(Long ticketId) {
        List<TicketMessage> messages = messageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        return messages.stream().map(this::mapToMessageResponse).collect(Collectors.toList());
    }

    @Override
    public void markMessagesAsRead(Long ticketId, Long userId) {
        messageRepository.markMessagesAsReadByUser(ticketId, userId);
        log.debug("Marked messages as read for ticket {} by user: {}", ticketId, userId);
    }

    @Override
    public AiSuggestedResponseDto generateAiResponse(Long ticketId, String userMessage) {
        // Placeholder for AI integration
        // In real implementation, this would integrate with AI service
        return AiSuggestedResponseDto.builder()
                .suggestedResponse("Thank you for contacting us. We're looking into your issue and will get back to you shortly.")
                .confidence(0.85)
                .suggestedActions(Arrays.asList("ACKNOWLEDGE", "REQUEST_INFO"))
                .knowledgeBaseArticleIds(Arrays.asList(1L, 2L, 3L))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KnowledgeBaseArticleDto> suggestKnowledgeBaseArticles(Long ticketId) {
        // Placeholder for knowledge base integration
        return Arrays.asList(
                KnowledgeBaseArticleDto.builder()
                        .id(1L)
                        .title("Common Delivery Issues")
                        .summary("How to resolve common delivery problems")
                        .relevanceScore(0.9)
                        .build()
        );
    }

    @Override
    public SupportTicketResponse rateTicket(Long ticketId, Integer rating, String feedback) {
        SupportTicket ticket = getTicketEntityById(ticketId);
        
        ticket.setCustomerSatisfactionRating(rating);
        ticket.setCustomerFeedback(feedback);
        ticket.setRatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());
        
        ticket = ticketRepository.save(ticket);
        
        logTicketActivity(ticket, "CUSTOMER_RATING", 
                String.format("Customer rated ticket: %d/5. Feedback: %s", rating, feedback));
        
        log.info("Customer rated ticket {} with rating: {}/5", ticketId, rating);
        return mapToResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTicketStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalTickets", ticketRepository.count());
        stats.put("openTickets", ticketRepository.findByStatus(SupportTicket.TicketStatus.OPEN, Pageable.unpaged()).getTotalElements());
        stats.put("inProgressTickets", ticketRepository.findByStatus(SupportTicket.TicketStatus.IN_PROGRESS, Pageable.unpaged()).getTotalElements());
        stats.put("closedTickets", ticketRepository.findByStatus(SupportTicket.TicketStatus.CLOSED, Pageable.unpaged()).getTotalElements());
        stats.put("escalatedTickets", (long)ticketRepository.findEscalatedTickets().size());
        stats.put("averageRating", ticketRepository.getAverageCustomerSatisfactionRating());
        stats.put("slaBreachedTickets", (long)ticketRepository.findSlaBreachedTickets().size());
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDeliveryRelatedStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalDeliveryTickets = ticketRepository.findDeliveryRelatedTickets(Pageable.unpaged()).getTotalElements();
        stats.put("totalDeliveryTickets", totalDeliveryTickets);
        stats.put("deliveryTicketPercentage", calculatePercentage(totalDeliveryTickets, ticketRepository.count()));
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAiPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long aiHandledTickets = ticketRepository.findAiHandledTickets(Pageable.unpaged()).getTotalElements();
        long totalTickets = ticketRepository.count();
        
        stats.put("aiHandledTickets", aiHandledTickets);
        stats.put("aiResolutionRate", calculatePercentage(aiHandledTickets, totalTickets));
        stats.put("targetAiResolutionRate", 67.2); // From UI specs
        
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> searchTickets(TicketSearchRequest searchRequest, Pageable pageable) {
        // Placeholder for advanced search implementation
        // In real implementation, this would use specifications or criteria API
        Page<SupportTicket> tickets = ticketRepository.findAll(pageable);
        return tickets.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getOverdueTickets(int overdueHours) {
        LocalDateTime overdueThreshold = LocalDateTime.now().minusHours(overdueHours);
        List<SupportTicket> tickets = ticketRepository.findOverdueTickets(overdueThreshold);
        return tickets.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getSlaBreachedTickets() {
        List<SupportTicket> tickets = ticketRepository.findSlaBreachedTickets();
        return tickets.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getEscalatedTickets() {
        List<SupportTicket> tickets = ticketRepository.findEscalatedTickets();
        return tickets.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public BulkOperationResult bulkAssignTickets(List<Long> ticketIds, Long agentId, String agentName) {
        List<Long> successfulIds = new ArrayList<>();
        List<BulkOperationResult.BulkOperationError> errors = new ArrayList<>();
        
        for (Long ticketId : ticketIds) {
            try {
                assignTicketToAgent(ticketId, agentId, agentName);
                successfulIds.add(ticketId);
            } catch (Exception e) {
                errors.add(BulkOperationResult.BulkOperationError.builder()
                        .ticketId(ticketId)
                        .errorMessage(e.getMessage())
                        .errorCode("ASSIGNMENT_FAILED")
                        .build());
            }
        }
        
        return BulkOperationResult.builder()
                .totalRequested(ticketIds.size())
                .successCount(successfulIds.size())
                .failureCount(errors.size())
                .successfulTicketIds(successfulIds)
                .errors(errors)
                .operationType("BULK_ASSIGN")
                .operationDetails(String.format("Assigned to agent: %s (%d)", agentName, agentId))
                .build();
    }

    @Override
    public BulkOperationResult bulkUpdateStatus(List<Long> ticketIds, SupportTicket.TicketStatus status) {
        List<Long> successfulIds = new ArrayList<>();
        List<BulkOperationResult.BulkOperationError> errors = new ArrayList<>();
        
        for (Long ticketId : ticketIds) {
            try {
                updateTicketStatus(ticketId, status, "Bulk update operation");
                successfulIds.add(ticketId);
            } catch (Exception e) {
                errors.add(BulkOperationResult.BulkOperationError.builder()
                        .ticketId(ticketId)
                        .errorMessage(e.getMessage())
                        .errorCode("STATUS_UPDATE_FAILED")
                        .build());
            }
        }
        
        return BulkOperationResult.builder()
                .totalRequested(ticketIds.size())
                .successCount(successfulIds.size())
                .failureCount(errors.size())
                .successfulTicketIds(successfulIds)
                .errors(errors)
                .operationType("BULK_STATUS_UPDATE")
                .operationDetails(String.format("Updated status to: %s", status))
                .build();
    }

    @Override
    public BulkOperationResult bulkUpdatePriority(List<Long> ticketIds, SupportTicket.TicketPriority priority) {
        List<Long> successfulIds = new ArrayList<>();
        List<BulkOperationResult.BulkOperationError> errors = new ArrayList<>();
        
        for (Long ticketId : ticketIds) {
            try {
                updateTicketPriority(ticketId, priority, "Bulk update operation");
                successfulIds.add(ticketId);
            } catch (Exception e) {
                errors.add(BulkOperationResult.BulkOperationError.builder()
                        .ticketId(ticketId)
                        .errorMessage(e.getMessage())
                        .errorCode("PRIORITY_UPDATE_FAILED")
                        .build());
            }
        }
        
        return BulkOperationResult.builder()
                .totalRequested(ticketIds.size())
                .successCount(successfulIds.size())
                .failureCount(errors.size())
                .successfulTicketIds(successfulIds)
                .errors(errors)
                .operationType("BULK_PRIORITY_UPDATE")
                .operationDetails(String.format("Updated priority to: %s", priority))
                .build();
    }

    @Override
    public byte[] exportTicketsToExcel(TicketExportRequest request) {
        // Placeholder for Excel export implementation
        log.info("Exporting tickets to Excel format");
        return new byte[0];
    }

    @Override
    public byte[] exportTicketsToPdf(TicketExportRequest request) {
        // Placeholder for PDF export implementation
        log.info("Exporting tickets to PDF format");
        return new byte[0];
    }

    @Override
    public SupportTicketResponse addTagToTicket(Long ticketId, String tagName) {
        SupportTicket ticket = getTicketEntityById(ticketId);
        ticket.getTags().add(tagName);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        ticket = ticketRepository.save(ticket);
        
        log.debug("Added tag '{}' to ticket: {}", tagName, ticketId);
        return mapToResponse(ticket);
    }

    @Override
    public SupportTicketResponse removeTagFromTicket(Long ticketId, String tagName) {
        SupportTicket ticket = getTicketEntityById(ticketId);
        ticket.getTags().remove(tagName);
        ticket.setUpdatedAt(LocalDateTime.now());
        
        ticket = ticketRepository.save(ticket);
        
        log.debug("Removed tag '{}' from ticket: {}", tagName, ticketId);
        return mapToResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagDto> getPopularTags() {
        // Placeholder for tag analytics implementation
        return Arrays.asList(
                TagDto.builder().name("delivery-issue").count(45L).build(),
                TagDto.builder().name("payment-problem").count(32L).build(),
                TagDto.builder().name("product-defect").count(28L).build()
        );
    }

    // ====================
    // PRIVATE HELPER METHODS
    // ====================

    private SupportTicket getTicketEntityById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));
    }

    private String generateTicketNumber() {
        return "SCT-" + System.currentTimeMillis();
    }

    private SupportTicket.TicketPriority determinePriority(CreateTicketRequest request) {
        if (request.getPriority() != null) {
            return request.getPriority();
        }
        
        // Auto-determine priority based on category and content
        if (request.getCategory() == SupportTicket.TicketCategory.DELIVERY && 
            request.getDeliveryIssueType() != null) {
            return SupportTicket.TicketPriority.HIGH;
        }
        
        return SupportTicket.TicketPriority.MEDIUM;
    }

    private LocalDateTime calculateSlaTargetTime(SupportTicket.TicketCategory category, SupportTicket.TicketPriority priority) {
        int hours = switch (priority) {
            case CRITICAL -> 2;
            case HIGH -> 4;
            case MEDIUM -> 24;
            case LOW -> 72;
        };
        
        return LocalDateTime.now().plusHours(hours);
    }

    private boolean shouldHandleWithAi(CreateTicketRequest request) {
        // Determine if ticket should be handled by AI based on category and content
        return request.getCategory() == SupportTicket.TicketCategory.GENERAL_INQUIRY ||
               request.getCategory() == SupportTicket.TicketCategory.ACCOUNT_ISSUES;
    }

    private void createInitialMessage(SupportTicket ticket, String content, Long customerId) {
        TicketMessage initialMessage = TicketMessage.builder()
                .ticket(ticket)
                .senderId(customerId)
                .senderName(ticket.getCustomerName())
                .senderType(TicketMessage.SenderType.CUSTOMER)
                .content(content)
                .isInternal(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        messageRepository.save(initialMessage);
    }

    private void attemptAiAutoResolution(SupportTicket ticket) {
        // Placeholder for AI auto-resolution logic
        log.debug("Attempting AI auto-resolution for ticket: {}", ticket.getId());
    }

    private SupportTicket.TicketPriority escalatePriority(SupportTicket.TicketPriority currentPriority) {
        return switch (currentPriority) {
            case LOW -> SupportTicket.TicketPriority.MEDIUM;
            case MEDIUM -> SupportTicket.TicketPriority.HIGH;
            case HIGH -> SupportTicket.TicketPriority.CRITICAL;
            case CRITICAL -> SupportTicket.TicketPriority.CRITICAL; // Already at highest
        };
    }

    private void logTicketActivity(SupportTicket ticket, String activityType, String description) {
        // Placeholder for activity logging
        log.info("Ticket {} activity: {} - {}", ticket.getId(), activityType, description);
    }

    private double calculatePercentage(long value, long total) {
        return total > 0 ? (double) value / total * 100.0 : 0.0;
    }

    // ====================
    // EVENT PUBLISHING METHODS
    // ====================

    private void publishTicketCreatedEvent(SupportTicket ticket) {
        try {
            kafkaTemplate.send("ticket-created", ticket.getId().toString(), mapToResponse(ticket));
        } catch (Exception e) {
            log.error("Failed to publish ticket created event for ticket: {}", ticket.getId(), e);
        }
    }

    private void publishTicketStatusChangedEvent(SupportTicket ticket, SupportTicket.TicketStatus previousStatus, String reason) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("ticketId", ticket.getId());
            event.put("previousStatus", previousStatus);
            event.put("newStatus", ticket.getStatus());
            event.put("reason", reason);
            event.put("timestamp", LocalDateTime.now());
            
            kafkaTemplate.send("ticket-status-changed", ticket.getId().toString(), event);
        } catch (Exception e) {
            log.error("Failed to publish ticket status changed event for ticket: {}", ticket.getId(), e);
        }
    }

    private void publishTicketAssignedEvent(SupportTicket ticket) {
        try {
            kafkaTemplate.send("ticket-assigned", ticket.getId().toString(), mapToResponse(ticket));
        } catch (Exception e) {
            log.error("Failed to publish ticket assigned event for ticket: {}", ticket.getId(), e);
        }
    }

    private void publishTicketEscalatedEvent(SupportTicket ticket) {
        try {
            kafkaTemplate.send("ticket-escalated", ticket.getId().toString(), mapToResponse(ticket));
        } catch (Exception e) {
            log.error("Failed to publish ticket escalated event for ticket: {}", ticket.getId(), e);
        }
    }

    private void publishTicketClosedEvent(SupportTicket ticket) {
        try {
            kafkaTemplate.send("ticket-closed", ticket.getId().toString(), mapToResponse(ticket));
        } catch (Exception e) {
            log.error("Failed to publish ticket closed event for ticket: {}", ticket.getId(), e);
        }
    }

    private void publishMessageCreatedEvent(SupportTicket ticket, TicketMessage message) {
        try {
            kafkaTemplate.send("ticket-message-created", ticket.getId().toString(), mapToMessageResponse(message));
        } catch (Exception e) {
            log.error("Failed to publish message created event for ticket: {}", ticket.getId(), e);
        }
    }

    // ====================
    // MAPPING METHODS
    // ====================

    private SupportTicketResponse mapToResponse(SupportTicket ticket) {
        return SupportTicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .customerId(ticket.getCustomerId())
                .customerEmail(ticket.getCustomerEmail())
                .customerName(ticket.getCustomerName())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .category(ticket.getCategory())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .channel(ticket.getChannel())
                .orderId(ticket.getOrderId())
                .orderNumber(ticket.getOrderNumber())
                .trackingNumber(ticket.getTrackingNumber())
                .deliveryIssueType(ticket.getDeliveryIssueType())
                .assignedAgentId(ticket.getAssignedToAgentId())
                .assignedAgentName(ticket.getAssignedToAgentName())
                .assignedTeamId(ticket.getAssignedToTeamId())
                .assignedTeamName(ticket.getAssignedToTeamName())
                .isEscalated(ticket.getEscalated())
                .escalationReason(ticket.getEscalationReason())
                .escalatedAt(ticket.getEscalatedAt())
                .resolutionNotes(ticket.getResolutionNotes())
                .customerRating(ticket.getCustomerSatisfactionRating())
                .customerFeedback(ticket.getCustomerFeedback())
                .isAiHandled(ticket.getAiHandled())
                .slaTargetTime(ticket.getSlaDueDate())
                .tags(new ArrayList<>(ticket.getTags()))
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .closedAt(ticket.getClosedAt())
                .assignedAt(ticket.getAssignedAt())
                .ratedAt(ticket.getRatedAt())
                .build();
    }

    private TicketMessageResponse mapToMessageResponse(TicketMessage message) {
        return TicketMessageResponse.builder()
                .id(message.getId())
                .ticketId(message.getTicket().getId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .senderType(message.getSenderType())
                .content(message.getContent())
                .isInternal(message.getIsInternal())
                .createdAt(message.getCreatedAt())
                .build();
    }
}