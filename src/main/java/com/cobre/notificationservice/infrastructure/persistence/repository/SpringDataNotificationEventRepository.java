package com.cobre.notificationservice.infrastructure.persistence.repository;

import com.cobre.notificationservice.domain.model.value.DeliveryStatus;
import com.cobre.notificationservice.infrastructure.persistence.entity.NotificationEventEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataNotificationEventRepository extends JpaRepository<NotificationEventEntity, String> {

    @Query("""
        select ne
        from NotificationEventEntity ne
        where ne.clientId = :clientId
          and (:fromDate is null or ne.eventCreatedAt >= :fromDate)
          and (:toDate is null or ne.eventCreatedAt <= :toDate)
          and (:status is null or ne.deliveryStatus = :status)
        order by ne.eventCreatedAt desc
        """)
    List<NotificationEventEntity> findByFilters(
            @Param("clientId") String clientId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            @Param("status") DeliveryStatus status);

    @Query("""
        select ne
        from NotificationEventEntity ne
        where ne.deliveryStatus = com.cobre.notificationservice.domain.model.value.DeliveryStatus.PENDING
           or (ne.deliveryStatus = com.cobre.notificationservice.domain.model.value.DeliveryStatus.FAILED_RETRYABLE
               and ne.nextRetryAt <= :now)
        order by ne.eventCreatedAt asc
        """)
    List<NotificationEventEntity> findDueForDelivery(@Param("now") Instant now);
}
