package com.example.demo.repository;
import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.payload.ChoiceVoteCount;
import com.example.demo.payload.UserSummary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    List<User> findByIdIn(List<Long> userIds);

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
    
    //List<User> findByEmailStartsWith(String email,Pageable pageable);
    
    //<User> findByUsernameStartsWith(String username,Pageable pageable);
    
    @Query("SELECT NEW com.example.demo.payload.UserSummary(u.id, u.username, u.name, u.photo) FROM User u WHERE u.email like :email% and u.isActive = True and u.isBlocked = False and u.id <> :userId")
    List<UserSummary> findByEmailStartsWith(@Param("email") String email,@Param("userId") Long userId,Pageable pageable);
    
    @Query("SELECT NEW com.example.demo.payload.UserSummary(u.id, u.username, u.name, u.photo) FROM User u WHERE u.username like :username% and u.isActive = True and u.isBlocked = False and u.id <> :userId")
    List<UserSummary> findByUsernameStartsWith(@Param("username") String username,@Param("userId") Long userId,Pageable pageable);
    
    @Query("SELECT NEW com.example.demo.payload.UserSummary(u.id, u.username, u.name, u.photo) FROM User u WHERE u.name like %:name% and u.isActive = True and u.isBlocked = False and u.id <> :userId ")
    List<UserSummary> findByNameContains(@Param("name") String name,@Param("userId") Long userId,Pageable pageable);

    //@Query("SELECT NEW com.example.demo.payload.UserSummary(u.id, u.username, u.name, u.photo) FROM User u ")
	Page<User> findAll (Pageable pageable);
}
