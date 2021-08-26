package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Post;
import com.example.demo.model.StoragePost;
import com.example.demo.model.User;
import com.example.demo.payload.UserIdCount;
@Repository
public interface StoragePostRepository  extends JpaRepository<StoragePost,Long>{
	@Query("SELECT s FROM StoragePost s WHERE s.post.id = :postId and s.user.id = :userId order by s.createdAt desc")
	StoragePost findByUserIdAndPostId(@Param("postId") Long postId,@Param("userId") Long userId);
	@Modifying
    @Query("delete from StoragePost v where v.id in (select v2.id from StoragePost v2 where v2.post.id = :postId)")
    int deleteStorageByPostId(@Param("postId") Long postId);
	
	@Query("SELECT COUNT(v.id) from StoragePost v where v.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
	
	@Query("SELECT NEW com.example.demo.payload.UserIdCount(v.user.id, count(v.user.id) as k) from StoragePost v where v.user.id in :userIds group by v.user.id order by k desc")
    List<UserIdCount> countByUserIdIn(@Param("userIds") List<Long> userIds);
	
	@Query("SELECT p.post FROM StoragePost p where p.user.id = :userId")
	Page<Post> findAllOfSave (@Param("userId")Long userId,Pageable pageable);
	
	@Query("SELECT p.user FROM StoragePost p where p.post.id = :postId")
	List<User> findAllUserByPostId(@Param("postId")Long postId);
}
