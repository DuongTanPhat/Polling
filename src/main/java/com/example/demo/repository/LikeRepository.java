package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.LikedPost;
import com.example.demo.payload.PostNumberCount;

public interface LikeRepository extends JpaRepository<LikedPost,Long>{
	@Query("SELECT NEW com.example.demo.payload.PostNumberCount(c.post.id, count(c.id)) FROM LikedPost c WHERE c.post.id in :postIds and c.isLike = True GROUP BY c.post.id")
    List<PostNumberCount> countByPostIdsInGroupByPostId(@Param("postIds") List<Long> postIds);
	@Query("SELECT c FROM LikedPost c WHERE c.post.id = :postId and c.user.id=:userId and c.isLike=:isLike")
	List<LikedPost> findByPostIdAndUserIdAndIsLike(@Param("postId")Long postId, @Param("userId")Long userId, @Param("isLike")Boolean isLike,Pageable pageable);
	@Query("SELECT c FROM LikedPost c WHERE c.post.id = :postId and c.user.id=:userId")
	List<LikedPost> findByPostIdAndUserId(@Param("postId")Long postId, @Param("userId")Long userId,Pageable pageable);
	@Query("SELECT NEW com.example.demo.payload.PostNumberCount(c.post.id, count(c.id)) FROM LikedPost c WHERE c.post.id = :postId and c.isLike = True GROUP BY c.post.id")
    PostNumberCount countByPostId(@Param("postId") Long postId);
}
