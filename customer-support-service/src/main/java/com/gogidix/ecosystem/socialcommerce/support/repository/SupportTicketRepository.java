package com.gogidix.ecosystem.socialcommerce.support.repository;

import com.gogidix.ecosystem.socialcommerce.support.entity.SupportTicket;
import com.gogidix.ecosystem.socialcommerce.support.entity.SupportTicket.TicketStatus;
import com.gogidix.ecosystem.socialcommerce.support.entity.SupportTicket.TicketPriority;
import com.gogidix.ecosystem.socialcommerce.support.entity.SupportTicket.TicketCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Support Ticket operations
 */
@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long>, JpaSpecificationExecutor<SupportTicket> {

    Optional<SupportTicket> findByTicketNumber(String ticketNumber);

    Page<SupportTicket> findByCustomerId(Long customerId, Pageable pageable);

    Page<SupportTicket> findByAssignedToAgentId(Long agentId, Pageable pageable);

    Page<SupportTicket> findByAssignedToTeamId(Long teamId, Pageable pageable);

    Page<SupportTicket> findByStatus(TicketStatus status, Pageable pageable);

    Page<SupportTicket> findByPriority(TicketPriority priority, Pageable pageable);

    Page<SupportTicket> findByCategory(TicketCategory category, Pageable pageable);

    Page<SupportTicket> findByAssignedToAgentIdIsNull(Pageable pageable);

    @Query("SELECT t FROM SupportTicket t WHERE t.status IN ('OPEN', 'IN_PROGRESS') " +
           "AND t.createdAt < :overdueThreshold")
    List<SupportTicket> findOverdueTickets(@Param("overdueThreshold") LocalDateTime overdueThreshold);

    @Query("SELECT t FROM SupportTicket t WHERE t.slaBreached = true AND t.status NOT IN ('RESOLVED', 'CLOSED')")
    List<SupportTicket> findSlaBreachedTickets();

    @Query("SELECT t.status, COUNT(t) FROM SupportTicket t GROUP BY t.status")
    List<Object[]> getTicketStatsByStatus();

    @Query("SELECT t.priority, COUNT(t) FROM SupportTicket t GROUP BY t.priority")
    List<Object[]> getTicketStatsByPriority();

    @Query("SELECT t.category, COUNT(t) FROM SupportTicket t GROUP BY t.category")
    List<Object[]> getTicketStatsByCategory();

    @Query("SELECT t FROM SupportTicket t WHERE t.customerRegion = :region AND t.status = :status")
    Page<SupportTicket> findByCustomerRegionAndStatus(@Param("region") String region, 
                                                      @Param("status") TicketStatus status, 
                                                      Pageable pageable);

    // Delivery-related queries
    @Query("SELECT t FROM SupportTicket t WHERE t.orderId = :orderId")
    List<SupportTicket> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT t FROM SupportTicket t WHERE t.trackingNumber = :trackingNumber")
    List<SupportTicket> findByTrackingNumber(@Param("trackingNumber") String trackingNumber);

    @Query("SELECT t FROM SupportTicket t WHERE t.deliveryIssueType IS NOT NULL")
    Page<SupportTicket> findDeliveryRelatedTickets(Pageable pageable);

    @Query("SELECT t.deliveryIssueType, COUNT(t) FROM SupportTicket t " +
           "WHERE t.deliveryIssueType IS NOT NULL GROUP BY t.deliveryIssueType")
    List<Object[]> getDeliveryIssueStats();

    // AI-related queries
    @Query("SELECT t FROM SupportTicket t WHERE t.aiHandled = true")
    Page<SupportTicket> findAiHandledTickets(Pageable pageable);

    @Query("SELECT AVG(t.aiConfidenceScore) FROM SupportTicket t WHERE t.aiHandled = true")
    Double getAverageAiConfidenceScore();

    // Performance queries
    @Query("SELECT AVG(t.responseTimeMinutes) FROM SupportTicket t WHERE t.responseTimeMinutes IS NOT NULL")
    Double getAverageResponseTime();

    @Query("SELECT AVG(t.resolutionTimeHours) FROM SupportTicket t WHERE t.resolutionTimeHours IS NOT NULL")
    Double getAverageResolutionTime();

    @Query("SELECT AVG(t.customerSatisfactionRating) FROM SupportTicket t WHERE t.customerSatisfactionRating IS NOT NULL")
    Double getAverageCustomerSatisfactionRating();

    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.createdAt >= :startDate AND t.createdAt <= :endDate")
    Long countTicketsInDateRange(@Param("startDate") LocalDateTime startDate, 
                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t.channel, COUNT(t) FROM SupportTicket t GROUP BY t.channel")
    List<Object[]> getTicketStatsByChannel();

    // Agent performance queries
    @Query("SELECT t.assignedToAgentId, COUNT(t), AVG(t.resolutionTimeHours), AVG(t.customerSatisfactionRating) " +
           "FROM SupportTicket t WHERE t.assignedToAgentId IS NOT NULL " +
           "GROUP BY t.assignedToAgentId")
    List<Object[]> getAgentPerformanceStats();

    // Regional performance queries
    @Query("SELECT t.customerRegion, COUNT(t), AVG(t.responseTimeMinutes), AVG(t.customerSatisfactionRating) " +
           "FROM SupportTicket t WHERE t.customerRegion IS NOT NULL " +
           "GROUP BY t.customerRegion")
    List<Object[]> getRegionalPerformanceStats();

    // Search functionality
    @Query("SELECT t FROM SupportTicket t WHERE " +
           "LOWER(t.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.ticketNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.customerEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<SupportTicket> searchTickets(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Escalation queries
    @Query("SELECT t FROM SupportTicket t WHERE t.status = 'ESCALATED'")
    List<SupportTicket> findEscalatedTickets();

    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.status = 'ESCALATED' AND t.assignedToAgentId = :agentId")
    Long countEscalatedTicketsByAgent(@Param("agentId") Long agentId);
}