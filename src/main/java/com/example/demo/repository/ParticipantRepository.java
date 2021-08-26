package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Participant;
import com.example.demo.payload.GroupResponse;
import com.example.demo.payload.UserSummary;
import com.example.demo.model.Group;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
	@Query("SELECT NEW com.example.demo.payload.GroupResponse(p.group.id, p.group.name , p.group.groupCode) FROM Participant p where p.user.id = :userId and p.group.groupCode like :name%")
	List<GroupResponse> findGroupbyName (@Param("name")String name,@Param("userId")Long userId,Pageable pageable);
	@Query("SELECT NEW com.example.demo.payload.GroupResponse(p.group.id, p.group.name , p.group.groupCode,p.group.createdAt,p.group.groupAdmin.username) FROM Participant p where p.user.id = :userId")
	Page<GroupResponse> findGroupByUser (@Param("userId")Long userId,Pageable pageable);
	@Query("SELECT NEW com.example.demo.payload.UserSummary(u.id, u.username, u.name, u.photo) FROM User u WHERE u.username like :username% and u.isActive = True and u.isBlocked = False and u.id <> :userId and u.id not in (select p.user.id from Participant p where p.group.groupCode = :code)")
	List<UserSummary> findByUsernameNotInStartsWith(@Param("username") String username,@Param("code")String code,@Param("userId") Long userId,Pageable pageable);
	@Query("SELECT p FROM Participant p WHERE p.user.id = :userId and p.group.id = :groupId")
	Participant findByUserIdAndGroupId(@Param("userId") Long userId, @Param("groupId") Long groupId);
}
