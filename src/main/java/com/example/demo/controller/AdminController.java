package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.exception.BadRequestException;
import com.example.demo.payload.PagedResponse;
import com.example.demo.payload.PostResponse;
import com.example.demo.payload.UserProfile;
import com.example.demo.payload.UserSummary;
import com.example.demo.repository.PasswordTokenRepository;
import com.example.demo.repository.PollRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VoteRepository;
import com.example.demo.security.CurrentUser;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.PostService;
import com.example.demo.util.AppConstants;

import com.example.demo.exception.AppException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Role;
import com.example.demo.model.RoleName;
import com.example.demo.model.User;
import com.example.demo.payload.ApiResponse;
import com.example.demo.payload.GroupResponse;
@RestController
@RequestMapping("/api/admin")
public class AdminController {
	@Autowired
    private UserRepository userRepository;

    @Autowired
    private PollRepository pollRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PostService postService;
    
    @Autowired
    private PasswordTokenRepository passwordResetTokenRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private JavaMailSender emailSender;
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserProfile> getAllUserProfile(@CurrentUser UserPrincipal currentUser) {
    	return postService.getAllUser();
    }
    @GetMapping("/groups")
    @PreAuthorize("hasRole('ADMIN')")
    public List<GroupResponse> getAllGroupResponse(@CurrentUser UserPrincipal currentUser) {
    	return postService.getAllGroup();
    }
    @GetMapping("/setadmin/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> setAdmin(@CurrentUser UserPrincipal currentUser,@PathVariable Long userId){
    	User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
		Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new AppException("ADMIN Role not set."));

		 Boolean isAdmin = user.addRole(adminRole);
		 if(isAdmin == false) return new ResponseEntity(new ApiResponse(false, "User already admin!"),
	                HttpStatus.BAD_REQUEST);
        try {
			userRepository.save(user);
		} catch (DataIntegrityViolationException e) {
			throw new BadRequestException("Set Admin Error!");
		}
		return new ResponseEntity(new ApiResponse(true, "Set admin Successfully!"),
                HttpStatus.OK);
	}
    @GetMapping("/removeadmin/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> removeAdmin(@CurrentUser UserPrincipal currentUser,@PathVariable Long userId){
    	if(currentUser.getId()==userId) return new ResponseEntity(new ApiResponse(false, "Can't remove Admin for yourself!"),
                HttpStatus.BAD_REQUEST);
    	User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
		Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new AppException("ADMIN Role not set."));

        Boolean isAdmin = user.removeRole(adminRole);
        if(isAdmin == false) return new ResponseEntity(new ApiResponse(false, "User not yet admin!"),
                HttpStatus.BAD_REQUEST);
        try {
			userRepository.save(user);
		} catch (DataIntegrityViolationException e) {
			throw new BadRequestException("Drop Admin Error!");
		}
		return new ResponseEntity(new ApiResponse(true, "Remove admin Successfully!"),
                HttpStatus.OK);
	}
    @GetMapping("/blocked/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> setBlocked(@CurrentUser UserPrincipal currentUser,@PathVariable Long userId){
    	if(currentUser.getId()==userId) return new ResponseEntity(new ApiResponse(false, "Can't block yourself!"),
                HttpStatus.BAD_REQUEST);
    	User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

		 user.setBlocked(true);
        try {
			userRepository.save(user);
		} catch (DataIntegrityViolationException e) {
			throw new BadRequestException("Blocked Error!");
		}
		return new ResponseEntity(new ApiResponse(true, "Block User Successfully!"),
                HttpStatus.OK);
	}
    @GetMapping("/unblocked/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> removeBlocked(@CurrentUser UserPrincipal currentUser,@PathVariable Long userId){
    	User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
		
        user.setBlocked(false);
        
        try {
			userRepository.save(user);
		} catch (DataIntegrityViolationException e) {
			throw new BadRequestException("Drop Admin Error!");
		}
		return new ResponseEntity(new ApiResponse(true, "UnBlock User Successfully!"),
                HttpStatus.OK);
	}
    @DeleteMapping("/groups/{groupId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> deleteGroup(@CurrentUser UserPrincipal currentUser,@PathVariable Long groupId){
    	postService.deleteGroup(groupId);
		return new ResponseEntity(new ApiResponse(true, "Delete Group Successfully!"),
                HttpStatus.OK);
	}
    @GetMapping("/posts")
	@PreAuthorize("hasRole('ADMIN')")
	public PagedResponse<PostResponse> getAllPolls(@CurrentUser UserPrincipal currentUser,@RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
			@RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size){
    	return postService.getAllPollsAdmin(page,size);
	}
    @DeleteMapping("/posts/{postId}")
   	@PreAuthorize("hasRole('ADMIN')")
   	public ResponseEntity<?> deletePost(@CurrentUser UserPrincipal currentUser,@PathVariable Long postId){
       	postService.deletePostAdmin(postId);
   		return new ResponseEntity(new ApiResponse(true, "Delete Post Successfully!"),
                   HttpStatus.OK);
   	}
}
