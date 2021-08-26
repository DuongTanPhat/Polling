package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Comment;
import com.example.demo.payload.PostNumberCount;

public interface CommentRepository extends JpaRepository<Comment,Long>{
	@Query("SELECT NEW com.example.demo.payload.PostNumberCount(c.post.id, count(c.id)) FROM Comment c WHERE c.post.id in :postIds GROUP BY c.post.id")
    List<PostNumberCount> countByPostIdsInGroupByPostId(@Param("postIds") List<Long> postIds);
	@Query("SELECT NEW com.example.demo.payload.PostNumberCount(c.post.id, count(c.id)) FROM Comment c WHERE c.post.id = :postId GROUP BY c.post.id")
    PostNumberCount countByPostId(@Param("postId") Long postId);
	@Query("SELECT c FROM Comment c WHERE c.post.id = :postId")
	Page<Comment> getCommentByPostId(@Param("postId") Long postId,Pageable pageable);
}
