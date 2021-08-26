package com.example.demo.repository;

import com.example.demo.payload.ChoiceVoteCount;
import com.example.demo.payload.ExPost;
import com.example.demo.payload.UserCountVoted;
import com.example.demo.payload.UserIdCount;
import com.example.demo.payload.UserSummary;
import com.example.demo.model.Post;
import com.example.demo.model.Vote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    @Query("SELECT NEW com.example.demo.payload.ChoiceVoteCount(v.choice.id, count(v.id)) FROM Vote v WHERE v.choice.poll.id in :pollIds GROUP BY v.choice.id")
    List<ChoiceVoteCount> countByPollIdInGroupByChoiceId(@Param("pollIds") List<Long> pollIds);
    
    @Query("SELECT NEW com.example.demo.payload.ChoiceVoteCount(v.choice.id, count(v.id)) FROM Vote v WHERE v.choice.poll.id in :pollIds and v.user.id = :userId GROUP BY v.choice.id")
    List<ChoiceVoteCount> countByPollIdInGroupByChoiceIdByUserId(@Param("pollIds") List<Long> pollIds,@Param("userId") Long userId);

    @Query("SELECT NEW com.example.demo.payload.UserSummary(v.user.id, v.user.username,v.user.name,v.user.photo) FROM Vote v WHERE v.choice.id = :choiceId")
    List<UserSummary> findUserSummarysByChoiceId(@Param("choiceId") Long choiceId);
   
    @Query("SELECT count(v.id) FROM Vote v WHERE v.choice.id = :choiceId GROUP BY v.choice.id")
    Long countByPollIdByChoiceId(@Param("choiceId") Long choiceId);
    
    @Query("SELECT count(v.id) FROM Vote v WHERE v.choice.id = :choiceId and v.user.id = :userId GROUP BY v.choice.id")
    Long countByPollIdByChoiceIdByUserId(@Param("choiceId") Long choiceId,@Param("userId") Long userId);
    //
//    @Query("SELECT NEW com.example.demo.payload.ChoiceVoteCount(v.choice.id, count(v.id)) FROM Vote v WHERE v.poll.id = :pollId GROUP BY v.choice.id")
//    List<ChoiceVoteCount> countByPollIdGroupByChoiceId(@Param("pollId") Long pollId);
//
//    @Query("SELECT v FROM Vote v where v.user.id = :userId and v.poll.id in :pollIds")
//    List<Vote> findByUserIdAndPollIdIn(@Param("userId") Long userId, @Param("pollIds") List<Long> pollIds);
//
    @Query("SELECT v FROM Vote v where v.user.id = :userId and v.choice.id = :choiceId")
    List<Vote> findByUserIdAndChoiceId(@Param("userId") Long userId, @Param("choiceId") Long choiceId);
//    
    @Query("SELECT v FROM Vote v where v.user.id = :userId and v.choice.poll.id = :pollId")
    List<Vote> findByUserIdAndPollId(@Param("userId") Long userId, @Param("pollId") Long pollId);
//
    @Query("SELECT COUNT(v.id) from Vote v where v.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    @Query("SELECT NEW com.example.demo.payload.UserIdCount(v.user.id, count(v.user.id) as k) from Vote v where v.user.id in :userIds group by v.user.id order by k desc")
    List<UserIdCount> countByUserIdIn(@Param("userIds") List<Long> userIds);
//
//    @Query("SELECT v.choice.poll.post FROM Vote v WHERE v.user.id = :userIdFind and (v.choice.poll.post.publicDate < CONVERT (datetime, GETDATE()) or v.choice.poll.post.publicDate IS NULL) and (v.choice.poll.post.showCase = 3 or (v.choice.poll.post.showCase = 4 and EXISTS(select m from v.choice.poll.post.relationships m where m.user.id=:userId)) or (v.choice.poll.post.showCase = 5 and EXISTS(select g from v.choice.poll.post.groupPostings g where EXISTS(select k from g.group.participants k where k.user.id=:userId)) )) group by v.choice.poll.post.id")
//    Page<Post> findVotedPostByUserId(@Param("userId") Long userId,@Param("userIdFind") Long userIdFind, Pageable pageable);
    @Query("SELECT NEW com.example.demo.payload.ExPost(v.choice.poll.post, MAX(v.createdAt)) FROM Vote v WHERE v.user.id = :userIdFind and (v.choice.poll.post.publicDate < CONVERT (datetime, GETDATE()) or v.choice.poll.post.publicDate IS NULL) and (v.choice.poll.post.showCase = 3 or (v.choice.poll.post.showCase = 4 and EXISTS(select m from v.choice.poll.post.relationships m where m.user.id=:userId)) or (v.choice.poll.post.showCase = 5 and EXISTS(select g from v.choice.poll.post.groupPostings g where EXISTS(select k from g.group.participants k where k.user.id=:userId)) )) group by v.choice.poll.post.id  ORDER BY MAX(v.createdAt) DESC")
    Page<ExPost> findVotedPostByUserId(@Param("userId") Long userId,@Param("userIdFind") Long userIdFind, Pageable pageable);
    
    @Query("SELECT NEW com.example.demo.payload.ExPost(v.choice.poll.post, MAX(v.createdAt)) FROM Vote v   WHERE v.user.id = :userId GROUP BY v.choice.poll.post.id ORDER BY MAX(v.createdAt) DESC")
    Page<ExPost> findVotedPostByMe(@Param("userId") Long userId, Pageable pageable);
//    @Query("SELECT v.choice.poll.post FROM Vote v   WHERE v.user.id = :userId order by v.createdAt DESC")
//    Page<Post> findVotedPostByMe(@Param("userId") Long userId, Pageable pageable);
//    
    @Query("SELECT v FROM Vote v where v.choice.id = :choiceId")
    List<Vote> findByChoiceId(@Param("choiceId") Long choiceId);
    
    @Query("SELECT NEW com.example.demo.payload.UserCountVoted(v.user, count(v.user) as k) FROM Vote v WHERE v.choice.id = :choiceId GROUP BY v.user order by k DESC")
    Page<UserCountVoted> findUserVoteByChoiceId(@Param("choiceId") Long choiceId,Pageable pageable);

    @Modifying
    @Query("delete from Vote v where v.id in (select v2.id from Vote v2 where v2.choice.poll.post.id = :postId)")
    int deleteVoteByPostId(@Param("postId") Long postId);
}
