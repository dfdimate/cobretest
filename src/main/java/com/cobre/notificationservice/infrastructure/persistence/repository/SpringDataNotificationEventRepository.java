package com.cobre.notificationservice.infrastructure.persistence.repository;

import com.cobre.notificationservice.infrastructure.persistence.entity.NotificationEventEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SpringDataNotificationEventRepository extends JpaRepository<NotificationEventEntity, String>,
        JpaSpecificationExecutor<NotificationEventEntity> {

    @Query("""
        select ne.notificationEventId
        from NotificationEventEntity ne
        where ne.deliveryStatus = com.cobre.notificationservice.domain.model.value.DeliveryStatus.PENDING
           or (ne.deliveryStatus = com.cobre.notificationservice.domain.model.value.DeliveryStatus.FAILED_RETRYABLE
               and ne.nextRetryAt <= :now)
        order by ne.eventCreatedAt asc
        """)
    List<String> findDueForDelivery(@Param("now") Instant now);

    @Query("""
        select ne
        from NotificationEventEntity ne
        where ne.deliveryStatus = com.cobre.notificationservice.domain.model.value.DeliveryStatus.DELIVERING
          and ne.deliveryClaimedAt <= :claimedBefore
        order by ne.deliveryClaimedAt asc
        """)
    List<NotificationEventEntity> findStaleDeliveries(@Param("claimedBefore") Instant claimedBefore);

    @Transactional
    @Modifying
    @Query("""
        update NotificationEventEntity ne
        set ne.deliveryStatus = com.cobre.notificationservice.domain.model.value.DeliveryStatus.DELIVERING,
            ne.lastAttemptAt = :claimedAt,
            ne.attemptCount = ne.attemptCount + 1,
            ne.nextRetryAt = null,
            ne.updatedAt = :claimedAt,
            ne.deliveryClaimedAt = :claimedAt
        where ne.notificationEventId = :notificationEventId
          and (ne.deliveryStatus = com.cobre.notificationservice.domain.model.value.DeliveryStatus.PENDING
               or (ne.deliveryStatus = com.cobre.notificationservice.domain.model.value.DeliveryStatus.FAILED_RETRYABLE
                   and ne.nextRetryAt <= :now))
        """)
    int claimForDelivery(
            @Param("notificationEventId") String notificationEventId,
            @Param("claimedAt") Instant claimedAt,
            @Param("now") Instant now);

    @Transactional
    @Modifying
    @Query("""
        update NotificationEventEntity ne
        set ne.deliveryStatus = com.cobre.notificationservice.domain.model.value.DeliveryStatus.PENDING,
            ne.nextRetryAt = null,
            ne.httpStatus = null,
            ne.finalFailureReason = null,
            ne.deliveredAt = null,
            ne.updatedAt = :requeuedAt,
            ne.deliveryClaimedAt = null
        where ne.notificationEventId = :notificationEventId
          and ne.deliveryStatus = com.cobre.notificationservice.domain.model.value.DeliveryStatus.FAILED
        """)
    int requeueIfFailed(
            @Param("notificationEventId") String notificationEventId,
            @Param("requeuedAt") Instant requeuedAt);
}
