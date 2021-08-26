package com.example.demo.controller;

import java.net.URI;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.demo.exception.BadRequestException;
import com.example.demo.model.Group;
import com.example.demo.model.Post;
import com.example.demo.payload.ApiResponse;
import com.example.demo.payload.GroupRequest;
import com.example.demo.payload.GroupResponse;
import com.example.demo.payload.PagedResponse;
import com.example.demo.payload.PostRequest;
import com.example.demo.payload.PostResponse;
import com.example.demo.payload.UserIdentityAvailability;
import com.example.demo.payload.UserSummary;
import com.example.demo.repository.GroupRepository;
import com.example.demo.security.CurrentUser;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.PostService;
import com.example.demo.util.AppConstants;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
	@Autowired
    private PostService postService;
	@Autowired
	private GroupRepository groupRepository;
	@PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createGroup(@CurrentUser UserPrincipal currentUser,@Valid @RequestBody GroupRequest groupRequest) {
        Group group = postService.createGroup(currentUser,groupRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{groupCode}")
                .buildAndExpand(group.getGroupCode()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Group Created Successfully"));
    }
	 @GetMapping("/find")
	    public List<GroupResponse> getListGroupResponse(@CurrentUser UserPrincipal currentUser,
	                                                @RequestParam(value = "name") String name) {
	    		return postService.getListGroupFromName(name,currentUser.getId());
	        
	    }
	    @GetMapping("/findadd")
	    public List<UserSummary> getListUserSummary(@CurrentUser UserPrincipal currentUser,
	    		@RequestParam(value = "code") String code,
	                                                @RequestParam(value = "username", required = false) String username) {
	    	if(username!=null) {
	    		return postService.getListUserForAddGroupFromUsername(username,code,currentUser.getId());
	    	}

	    	throw new BadRequestException("User not found");
	        
	    }
	 @GetMapping("/edit")
	    public GroupResponse getGroupResponse(@CurrentUser UserPrincipal currentUser,
	                                                @RequestParam(value = "code") String code) {
	    		return postService.getGroupFromCode(code,currentUser.getId());
	        
	    }
	 @PostMapping("/edit")
	    @PreAuthorize("hasRole('USER')")
	    public ResponseEntity<?> editGroup(@CurrentUser UserPrincipal currentUser,@Valid @RequestBody GroupRequest groupRequest) {
	        Group group = postService.editGroup(currentUser,groupRequest);

	        URI location = ServletUriComponentsBuilder
	                .fromCurrentRequest().path("/{groupCode}")
	                .buildAndExpand(group.getGroupCode()).toUri();

	        return ResponseEntity.created(location)
	                .body(new ApiResponse(true, "Group Update Successfully"));
	    }
	 @DeleteMapping("/{groupId}/user")
		@PreAuthorize("hasRole('USER')")
		public ResponseEntity<?> deleteUserGroup(@CurrentUser UserPrincipal currentUser,@PathVariable Long groupId, @RequestParam(value = "user") Long userId){
	    System.out.println("c");
	    System.out.println(groupId+"  "+ userId);
		 postService.deleteUserGroupByGroupAdmin(groupId,userId,currentUser);
			return new ResponseEntity(new ApiResponse(true, "Remove User Successfully!"),
	                HttpStatus.OK);
		}
	 @DeleteMapping("/{groupId}/leave")
		@PreAuthorize("hasRole('USER')")
		public ResponseEntity<?> leaveGroup(@CurrentUser UserPrincipal currentUser,@PathVariable Long groupId){
	    	postService.leaveGroup(groupId,currentUser);
			return new ResponseEntity(new ApiResponse(true, "Leave Group Successfully!"),
	                HttpStatus.OK);
		}
	 @DeleteMapping("/{groupId}")
		@PreAuthorize("hasRole('USER')")
		public ResponseEntity<?> deleteGroup(@CurrentUser UserPrincipal currentUser,@PathVariable Long groupId){
	    	postService.deleteGroupByGroupAdmin(groupId,currentUser);
			return new ResponseEntity(new ApiResponse(true, "Delete Group Successfully!"),
	                HttpStatus.OK);
		}
	 @GetMapping
	 @PreAuthorize("hasRole('USER')")
	    public PagedResponse<GroupResponse> getListGroup(@CurrentUser UserPrincipal currentUser,@RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
	    		return postService.getListGroup(currentUser.getId(),page,size);
	        
	    }
	 @GetMapping("/polls")
	    public PagedResponse<PostResponse> getPolls(@CurrentUser UserPrincipal currentUser,
	    											@RequestParam(value= "code") String code,
	                                                @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
	                                                @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
	        return postService.getAllPollsOfGroup(currentUser,code, page, size);
	    }
	 @GetMapping("/checkGroupCodeAvailability")
	    public UserIdentityAvailability checkGroupCodeAvailability(@RequestParam(value = "code") String code) {
	        Boolean isAvailable = !groupRepository.existsByGroupCode(code);
	        return new UserIdentityAvailability(isAvailable);
	    }
}
