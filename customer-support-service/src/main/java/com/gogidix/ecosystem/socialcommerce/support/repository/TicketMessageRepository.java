package com.gogidix.ecosystem.socialcommerce.support.repository;

import com.gogidix.ecosystem.socialcommerce.support.entity.TicketMessage;
import com.gogidix.ecosystem.socialcommerce.support.entity.TicketMessage.MessageSender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Ticket Message operations
 */
@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {

    Page<TicketMessage> findBySupportTicketIdOrderBySentAtAsc(Long ticketId, Pageable pageable);

    List<TicketMessage> findBySupportTicketIdOrderBySentAtAsc(Long ticketId);

    List<TicketMessage> findBySupportTicketIdAndIsInternalFalseOrderBySentAtAsc(Long ticketId);

    List<TicketMessage> findBySupportTicketIdAndSenderOrderBySentAtAsc(Long ticketId, MessageSender sender);

    @Query("SELECT m FROM TicketMessage m WHERE m.supportTicket.id = :ticketId " +
           "AND m.sentAt > :afterTime ORDER BY m.sentAt ASC")
    List<TicketMessage> findMessagesAfterTime(@Param("ticketId") Long ticketId, 
                                             @Param("afterTime") LocalDateTime afterTime);

    @Query("SELECT COUNT(m) FROM TicketMessage m WHERE m.supportTicket.id = :ticketId AND m.isRead = false")
    Long countUnreadMessages(@Param("ticketId") Long ticketId);

    @Query("SELECT COUNT(m) FROM TicketMessage m WHERE m.supportTicket.id = :ticketId " +
           "AND m.sender = 'CUSTOMER' AND m.isRead = false")
    Long countUnreadCustomerMessages(@Param("ticketId") Long ticketId);

    @Query("SELECT m FROM TicketMessage m WHERE m.aiGenerated = true AND m.supportTicket.id = :ticketId")
    List<TicketMessage> findAiGeneratedMessages(@Param("ticketId") Long ticketId);

    @Query("SELECT AVG(m.aiConfidenceScore) FROM TicketMessage m WHERE m.aiGenerated = true " +
           "AND m.supportTicket.id = :ticketId")
    Double getAverageAiConfidenceForTicket(@Param("ticketId") Long ticketId);

    @Query("SELECT m.sentiment, COUNT(m) FROM TicketMessage m WHERE m.supportTicket.id = :ticketId " +
           "AND m.sentiment IS NOT NULL GROUP BY m.sentiment")
    List<Object[]> getSentimentDistributionForTicket(@Param("ticketId") Long ticketId);

    @Query("SELECT m FROM TicketMessage m WHERE m.markedAsAnswer = true AND m.supportTicket.id = :ticketId")
    List<TicketMessage> findMarkedAnswers(@Param("ticketId") Long ticketId);

    @Query("SELECT m FROM TicketMessage m WHERE m.supportTicket.id = :ticketId " +
           "AND m.messageType = 'SYSTEM_NOTE' ORDER BY m.sentAt DESC")
    List<TicketMessage> findSystemMessages(@Param("ticketId") Long ticketId);

    @Query("SELECT COUNT(m) FROM TicketMessage m WHERE m.senderId = :agentId " +
           "AND m.sender = 'AGENT' AND m.sentAt >= :startDate")
    Long countAgentMessagesInPeriod(@Param("agentId") Long agentId, 
                                   @Param("startDate") LocalDateTime startDate);

    @Query("SELECT AVG(m.helpfulnessRating) FROM TicketMessage m WHERE m.senderId = :agentId " +
           "AND m.sender = 'AGENT' AND m.helpfulnessRating IS NOT NULL")
    Double getAverageHelpfulnessRatingForAgent(@Param("agentId") Long agentId);

    @Query("SELECT m FROM TicketMessage m WHERE m.supportTicket.customerId = :customerId " +
           "AND m.sender = 'CUSTOMER' ORDER BY m.sentAt DESC")
    Page<TicketMessage> findCustomerMessages(@Param("customerId") Long customerId, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT m.supportTicket.id) FROM TicketMessage m " +
           "WHERE m.sentAt >= :startDate AND m.sentAt <= :endDate")
    Long countActiveTicketsInPeriod(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);

    // Translation queries
    @Query("SELECT DISTINCT m.originalLanguage FROM TicketMessage m WHERE m.originalLanguage IS NOT NULL")
    List<String> findDistinctLanguages();

    @Query("SELECT m.originalLanguage, COUNT(m) FROM TicketMessage m " +
           "WHERE m.originalLanguage IS NOT NULL GROUP BY m.originalLanguage")
    List<Object[]> getLanguageDistribution();
}