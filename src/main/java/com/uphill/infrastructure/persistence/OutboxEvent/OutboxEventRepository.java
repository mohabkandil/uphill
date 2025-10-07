package com.uphill.infrastructure.persistence.OutboxEvent;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {
    @Query(value = "SELECT * FROM outbox_events e WHERE e.status = 'PENDING' AND (e.next_retry_at IS NULL OR e.next_retry_at <= :now) ORDER BY e.created_at ASC FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<OutboxEventEntity> findPendingDueEvents(@Param("now") LocalDateTime now);
    
    List<OutboxEventEntity> findByAggregateId(Long aggregateId);
}


