package com.example.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Notification;
import com.example.demo.payload.NotificationResponse;


public interface NotificationRepository extends JpaRepository<Notification,Long> {
	@Query("SELECT v from Notification v where v.user.id = :userId")
	Page<Notification> findAllByUserId (@Param("userId")Long userId,Pageable pageable);
	@Query("SELECT NEW com.example.demo.payload.NotificationResponse(v.id, v.type,v.sourceId,v.isRead,v.content,v.createdAt) from Notification v where v.user.id = :userId")
	Page<NotificationResponse> findAllResponseByUserId (@Param("userId")Long userId,Pageable pageable);
	@Query("SELECT COUNT(v.id) from Notification v where v.user.id = :userId and isRead = False")
    Integer countByUserId(@Param("userId") Long userId);
}
