package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Group;
import com.example.demo.model.User;
import com.example.demo.payload.GroupResponse;
import com.example.demo.payload.UserIdCount;

public interface GroupRepository extends JpaRepository<Group, Long> {
	Optional<Group> findByGroupCode(String code);
	Boolean existsByGroupCode(String code);
	@Query("SELECT NEW com.example.demo.payload.GroupResponse(v.id,v.name,v.groupCode,v.createdAt,v.groupAdmin.username) from Group v")
    List<GroupResponse> findAllResponse();
}
