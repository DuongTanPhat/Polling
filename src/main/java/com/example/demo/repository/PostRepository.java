package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Post;
import com.example.demo.payload.ChoiceVoteCount;
import com.example.demo.payload.PostNumberCount;
import com.example.demo.payload.UserCountVoted;
import com.example.demo.payload.UserIdCount;

public interface PostRepository extends JpaRepository<Post, Long>{
	//@Query("SELECT p FROM Post p where p.showCase = 3 or (p.showCase = 4 and EXISTS(select v from p.relationships v where v.user.id=:userId)) or (p.showCase = 5 and EXISTS(select g from p.groupPostings.group.participants g where g.user.id=:userId))")
//	@Query("SELECT p FROM Post p where (p.publicDate<CONVERT (datetime, GETDATE()) or p.publicDate IS NULL) and (p.showCase = 3 or (p.showCase = 4 and EXISTS(select v from p.relationships v where v.user.id=:userId)) or (p.showCase = 5 and EXISTS(select g from p.groupPostings g where EXISTS(select k from g.group.participants k where k.user.id=:userId)) ))")
	@Query("SELECT p FROM Post p where (p.publicDate<current_timestamp() or p.publicDate IS NULL) and (p.showCase = 3 or (p.showCase = 4 and EXISTS(select v from p.relationships v where v.user.id=:userId)) or (p.showCase = 5 and EXISTS(select g from p.groupPostings g where EXISTS(select k from g.group.participants k where k.user.id=:userId)) ))")
	Page<Post> findAllOfUser (@Param("userId")Long userId,Pageable pageable);
//	@Query("SELECT p FROM Post p where (p.question like %:search%) and (p.publicDate<CONVERT (datetime, GETDATE()) or p.publicDate IS NULL) and (p.showCase = 3 or (p.showCase = 4 and EXISTS(select v from p.relationships v where v.user.id=:userId)) or (p.showCase = 5 and EXISTS(select g from p.groupPostings g where EXISTS(select k from g.group.participants k where k.user.id=:userId)) ))")
	@Query("SELECT p FROM Post p where (p.question like %:search%) and (p.publicDate<current_timestamp() or p.publicDate IS NULL) and (p.showCase = 3 or (p.showCase = 4 and EXISTS(select v from p.relationships v where v.user.id=:userId)) or (p.showCase = 5 and EXISTS(select g from p.groupPostings g where EXISTS(select k from g.group.participants k where k.user.id=:userId)) ))")
	Page<Post> findAllOfUserSearch (@Param("userId")Long userId,@Param("search")String search,Pageable pageable);
	//@Query("SELECT p FROM Post p where (p.publicDate<CONVERT (datetime, GETDATE()) or p.publicDate IS NULL) and p.showCase = 3 ")
	@Query("SELECT p FROM Post p where (p.publicDate<current_timestamp() or p.publicDate IS NULL) and p.showCase = 3 ")
	Page<Post> findAll (Pageable pageable);
	//@Query("SELECT p FROM Post p where (p.question like %:search%) and (p.publicDate<CONVERT (datetime, GETDATE()) or p.publicDate IS NULL) and p.showCase = 3 ")
	@Query("SELECT p FROM Post p where (p.question like %:search%) and (p.publicDate<current_timestamp() or p.publicDate IS NULL) and p.showCase = 3 ")
	Page<Post> findAllSearch (@Param("search")String search,Pageable pageable);
//	@Query("SELECT p FROM Post p where (p.id = :postId)and (p.publicDate<CONVERT (datetime, GETDATE()) or p.publicDate IS NULL) and (p.showCase = 2 or p.showCase = 3 or (p.showCase = 4 and EXISTS(select v from p.relationships v where v.user.id=:userId)) or (p.showCase = 5 and EXISTS(select g from p.groupPostings g where EXISTS(select k from g.group.participants k where k.user.id=:userId)) ))")
	@Query("SELECT p FROM Post p where (p.id = :postId)and (p.publicDate<current_timestamp() or p.publicDate IS NULL) and (p.showCase = 2 or p.showCase = 3 or (p.showCase = 4 and EXISTS(select v from p.relationships v where v.user.id=:userId)) or (p.showCase = 5 and EXISTS(select g from p.groupPostings g where EXISTS(select k from g.group.participants k where k.user.id=:userId)) ))")
	Page<Post> findByIdOfUser (@Param("userId")Long userId,@Param("postId")Long postId,Pageable pageable);
	@Query("SELECT p FROM Post p where (p.id = :postId)and (p.publicDate<current_timestamp() or p.publicDate IS NULL) and (p.showCase = 2 or p.showCase = 3 )")
	Page<Post> findByIdNoUser (@Param("postId")Long postId,Pageable pageable);
    @Query("SELECT COUNT(v.id) from Post v where v.createdBy = :userId")
    long countByCreatedBy(@Param("userId") Long userId);
    @Query("SELECT p FROM Post p")
	Page<Post> findAllForAdmin (Pageable pageable);
    @Query("SELECT NEW com.example.demo.payload.UserIdCount(v.createdBy, count(v.createdBy) as k) from Post v where v.createdBy in :userIds group by v.createdBy order by k desc")
    List<UserIdCount> countByCreatedByIn(@Param("userIds") List<Long> userIds);
    
    @Query("SELECT p FROM Post p where (p.createdBy = :userIdFind) and (p.publicDate<current_timestamp() or p.publicDate IS NULL) and (p.showCase = 3 or (p.showCase = 4 and EXISTS(select v from p.relationships v where v.user.id=:userId)) or (p.showCase = 5 and EXISTS(select g from p.groupPostings g where EXISTS(select k from g.group.participants k where k.user.id=:userId)) ))")
	Page<Post> findPollsCreatedByUser (@Param("userId")Long userId,@Param("userIdFind")Long userIdFind,Pageable pageable);
    
    @Query("SELECT p FROM Post p where p.createdBy = :userId")
   	Page<Post> findPollsCreatedByMe (@Param("userId")Long userId ,Pageable pageable);
    
}
