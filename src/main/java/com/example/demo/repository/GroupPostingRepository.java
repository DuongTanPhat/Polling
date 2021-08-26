package com.example.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.GroupPosting;
import com.example.demo.model.Post;

public interface GroupPostingRepository extends JpaRepository<GroupPosting,Long>{
	@Query("SELECT p.post FROM GroupPosting p where (p.post.publicDate<current_timestamp() or p.post.publicDate IS NULL) and (p.group.groupCode=:code)")
	Page<Post> findAllOfGroup (@Param("code")String code,Pageable pageable);
}
