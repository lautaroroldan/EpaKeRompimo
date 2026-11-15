package com.lautaroroldan.epakerompimo.repository;

import com.lautaroroldan.epakerompimo.entity.Notification;
import com.lautaroroldan.epakerompimo.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    void deleteByUser_IdUser(Long idUser);

    @Transactional
    @Modifying
    @Query("update Notification n set n.status = :status, n.sentAt = :sentAt, "
            + "n.errorMessage = :errorMessage where n.id = :id")
    void updateStatus(@Param("id") Long id,
                      @Param("status") NotificationStatus status,
                      @Param("sentAt") Instant sentAt,
                      @Param("errorMessage") String errorMessage);
}