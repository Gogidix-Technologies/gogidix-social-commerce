package com.gogidix.ecosystem.socialcommerce.support.service;

import com.gogidix.ecosystem.socialcommerce.support.dto.*;
import com.gogidix.ecosystem.socialcommerce.support.entity.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Service interface for Support Ticket operations
 */
public interface SupportTicketService {

    // Core ticket operations
    SupportTicketResponse createTicket(CreateTicketRequest request);
    
    SupportTicketResponse getTicketById(Long ticketId);
    
    SupportTicketResponse getTicketByNumber(String ticketNumber);
    
    Page<SupportTicketResponse> getCustomerTickets(Long customerId, Pageable pageable);
    
    Page<SupportTicketResponse> getAgentTickets(Long agentId, Pageable pageable);
    
    Page<SupportTicketResponse> getTeamTickets(Long teamId, Pageable pageable);
    
    Page<SupportTicketResponse> getTicketsByStatus(SupportTicket.TicketStatus status, Pageable pageable);
    
    Page<SupportTicketResponse> getTicketsByPriority(SupportTicket.TicketPriority priority, Pageable pageable);
    
    Page<SupportTicketResponse> getTicketsByCategory(SupportTicket.TicketCategory category, Pageable pageable);
    
    Page<SupportTicketResponse> getUnassignedTickets(Pageable pageable);
    
    // Ticket management
    SupportTicketResponse updateTicketStatus(Long ticketId, SupportTicket.TicketStatus status, String reason);
    
    SupportTicketResponse updateTicketPriority(Long ticketId, SupportTicket.TicketPriority priority, String reason);
    
    SupportTicketResponse updateTicketCategory(Long ticketId, SupportTicket.TicketCategory category);
    
    SupportTicketResponse assignTicketToAgent(Long ticketId, Long agentId, String agentName);
    
    SupportTicketResponse assignTicketToTeam(Long ticketId, Long teamId, String teamName);
    
    SupportTicketResponse escalateTicket(Long ticketId, String escalationReason);
    
    SupportTicketResponse closeTicket(Long ticketId, String resolutionNotes);
    
    SupportTicketResponse reopenTicket(Long ticketId, String reason);
    
    // Message operations
    TicketMessageResponse addMessageToTicket(Long ticketId, AddMessageRequest request);
    
    Page<TicketMessageResponse> getTicketMessages(Long ticketId, Pageable pageable);
    
    List<TicketMessageResponse> getTicketConversation(Long ticketId);
    
    void markMessagesAsRead(Long ticketId, Long userId);
    
    // AI-powered operations
    AiSuggestedResponseDto generateAiResponse(Long ticketId, String userMessage);
    
    List<KnowledgeBaseArticleDto> suggestKnowledgeBaseArticles(Long ticketId);
    
    SupportTicketResponse handleAiEscalation(Long ticketId, String escalationReason);
    
    // Customer satisfaction
    SupportTicketResponse rateTicket(Long ticketId, Integer rating, String feedback);
    
    // Analytics and reporting
    Map<String, Object> getTicketStatistics();
    
    Map<String, Object> getTicketStatisticsByDateRange(String startDate, String endDate);
    
    Map<String, Object> getAgentPerformanceStats(Long agentId);
    
    Map<String, Object> getRegionalPerformanceStats(String region);
    
    Map<String, Object> getDeliveryRelatedStats();
    
    Map<String, Object> getAiPerformanceStats();
    
    // Search and filtering
    Page<SupportTicketResponse> searchTickets(TicketSearchRequest searchRequest, Pageable pageable);
    
    List<SupportTicketResponse> getOverdueTickets(int overdueHours);
    
    List<SupportTicketResponse> getSlaBreachedTickets();
    
    List<SupportTicketResponse> getEscalatedTickets();
    
    // Order and delivery related
    List<SupportTicketResponse> getTicketsByOrderId(Long orderId);
    
    List<SupportTicketResponse> getTicketsByTrackingNumber(String trackingNumber);
    
    Page<SupportTicketResponse> getDeliveryRelatedTickets(Pageable pageable);
    
    // Tag management
    SupportTicketResponse addTagToTicket(Long ticketId, String tagName);
    
    SupportTicketResponse removeTagFromTicket(Long ticketId, String tagName);
    
    List<TagDto> getPopularTags();
    
    // Bulk operations
    BulkOperationResult bulkAssignTickets(List<Long> ticketIds, Long agentId, String agentName);
    
    BulkOperationResult bulkUpdateStatus(List<Long> ticketIds, SupportTicket.TicketStatus status);
    
    BulkOperationResult bulkUpdatePriority(List<Long> ticketIds, SupportTicket.TicketPriority priority);
    
    // Export and reporting
    byte[] exportTicketsToExcel(TicketExportRequest request);
    
    byte[] exportTicketsToPdf(TicketExportRequest request);
    
    // Integration with order tracking
    void handleOrderStatusUpdate(OrderStatusUpdateEvent event);
    
    void handleDeliveryUpdate(DeliveryUpdateEvent event);
    
    // Real-time notifications
    void notifyAgentOfNewTicket(Long agentId, Long ticketId);
    
    void notifyCustomerOfResponse(Long customerId, Long ticketId);
    
    void notifyManagerOfEscalation(Long ticketId, String reason);
}