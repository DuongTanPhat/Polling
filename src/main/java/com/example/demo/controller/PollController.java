package com.example.demo.controller;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.payload.*;
import com.example.demo.repository.PollRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VoteRepository;
import com.example.demo.security.CurrentUser;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.NotificationService;
import com.example.demo.service.PostService;
import com.example.demo.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/polls")
public class PollController {

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private VoteRepository voteRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PostService postService;
	@Autowired
	private NotificationService notificationService;

	private static final Logger logger = LoggerFactory.getLogger(PollController.class);

	@GetMapping
	public PagedResponse<PostResponse> getPolls(@CurrentUser UserPrincipal currentUser,
			@RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
			@RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
		System.out.println(currentUser);
		if (currentUser == null) {
			return postService.getAllPolls(page, size);
		} else
			return postService.getAllPolls(currentUser, page, size);
	}
	@GetMapping("/search")
	public PagedResponse<PostResponse> getPollsSearch(@CurrentUser UserPrincipal currentUser,
			@RequestParam(value = "search") String search,
			@RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
			@RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
		System.out.println(currentUser);
		if (currentUser == null) {
			return postService.getAllPollsSearch(search,page, size);
		} else
			return postService.getAllPollsSearch(currentUser,search, page, size);
	}
	@GetMapping("/{postId}")
	public PagedResponse<PostResponse> getOnePoll(@CurrentUser UserPrincipal currentUser, @PathVariable Long postId) {
		if (currentUser == null) {
			return postService.getOnePollsNoUser(postId);
		} else
		return postService.getOnePolls(currentUser, postId);
	}
	  @DeleteMapping("/{postId}")
	    public ResponseEntity<?> deletePost(@PathVariable Long postId,@CurrentUser UserPrincipal currentUser) {
		  boolean suc = postService.deletePost(postId, currentUser);
		  if (suc) return new ResponseEntity(new ApiResponse(true, "Delete Successfully!"),
	                HttpStatus.OK);
		  else return new ResponseEntity(new ApiResponse(true, "Delete Failed!"),
	                HttpStatus.BAD_REQUEST);
	  }
	  @DeleteMapping("/{postId}/comment/{commentId}")
	    public ResponseEntity<?> deleteComment(@PathVariable Long postId,@PathVariable Long commentId,@CurrentUser UserPrincipal currentUser) {
		  boolean suc = postService.deleteComment(postId, commentId, currentUser);
		  if (suc) return new ResponseEntity(new ApiResponse(true, "Delete Successfully!"),
	                HttpStatus.OK);
		  else return new ResponseEntity(new ApiResponse(true, "Delete Failed!"),
	                HttpStatus.BAD_REQUEST);
	  }
	  @DeleteMapping("/{postId}/comment/{commentId}/reply/{replyId}")
	    public ResponseEntity<?> deleteReply(@PathVariable Long postId,@PathVariable Long commentId,@PathVariable Long replyId,@CurrentUser UserPrincipal currentUser) {
		  boolean suc = postService.deleteReply(postId, commentId, replyId, currentUser);
		  if (suc) return new ResponseEntity(new ApiResponse(true, "Delete Successfully!"),
	                HttpStatus.OK);
		  else return new ResponseEntity(new ApiResponse(true, "Delete Failed!"),
	                HttpStatus.BAD_REQUEST);
	  }
	  @PutMapping("/{postId}")
	  @PreAuthorize("hasRole('USER')")
	    public ResponseEntity<?> updateName(@CurrentUser UserPrincipal currentUser,@PathVariable Long postId,@RequestParam("name") String name){
	    	
	    	Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
	    	if(post.getCreatedBy()!=currentUser.getId()) return new ResponseEntity(new ApiResponse(false, "This post not create by you!"),
                    HttpStatus.BAD_REQUEST);
	    	post.setQuestion(name);
	    	
	    	try {
	    		postRepository.save(post);
	    		SocketNamePost socket = new SocketNamePost();
	    		socket.setType(9);
	    		socket.setPostId(postId);
	    		socket.setName(name);
	    		notificationService.dispatch(currentUser.getUsername(), socket, postId);
	    	}catch(Exception e) {
	    		return new ResponseEntity(new ApiResponse(false, "Change post name failed!"),
	                    HttpStatus.BAD_REQUEST);
	    	}
	    	
	    	return new ResponseEntity(new ApiResponse(true, "Change post name Successfully!"),
	                HttpStatus.OK);
	    }
//    @GetMapping("/")
//    public PagedResponse<PostResponse> getPollsForNonUser(
//                                                @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
//                                                @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
//        return postService.getAllPolls(page, size);
//    }
	@GetMapping("/voted")
	public PagedResponse<UserCountVoted> getListUserVotedFromChoice(@CurrentUser UserPrincipal currentUser,
			@RequestParam(value = "choice") Long choiceId,
			@RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
			@RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_USER_SIZE) int size) {
		return postService.getListUserVotedChoice(choiceId, currentUser, page, size);
	}

	@GetMapping("/{postId}/comment")
	public PagedResponse<CommentResponse> getListComment(@CurrentUser UserPrincipal currentUser,
			@PathVariable Long postId,
			@RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
			@RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_USER_SIZE) int size) {
		return postService.getCommentList(postId, currentUser, page, size);
	}
	@PostMapping("/{postId}/comment")
	public CommentResponse commentPost(@CurrentUser UserPrincipal currentUser,
			@PathVariable Long postId,@RequestParam(value = "text") String text) {
		return postService.commentPost(postId,text, currentUser);
	}
	@PostMapping("/{postId}/comment/{commentId}")
	public ReplyResponse replyComment(@CurrentUser UserPrincipal currentUser,
			@PathVariable Long postId,@PathVariable Long commentId,@RequestParam(value = "text") String text) {
		return postService.replyComment(postId, commentId, text, currentUser);
	}
	@PostMapping("/{postId}/save")
	public ResponseEntity<?> savePost(@CurrentUser UserPrincipal currentUser,
			@PathVariable Long postId) {
		boolean suc =  postService.storagePost(postId, currentUser);
		 if (suc) return new ResponseEntity(new ApiResponse(true, "Storage Successfully!"),
	                HttpStatus.OK);
		  else return new ResponseEntity(new ApiResponse(true, "Storage Failed!"),
	                HttpStatus.BAD_REQUEST);
	}
	@DeleteMapping("/{postId}/save")
	public ResponseEntity<?> deleteSavePost(@CurrentUser UserPrincipal currentUser,
			@PathVariable Long postId) {
		boolean suc =  postService.deleteStoragePost(postId, currentUser);
		 if (suc) return new ResponseEntity(new ApiResponse(true, "Delete Storage Successfully!"),
	                HttpStatus.OK);
		  else return new ResponseEntity(new ApiResponse(true, "Delete Storage Failed!"),
	                HttpStatus.BAD_REQUEST);
	}
//    @GetMapping("/aa")
//    public List<User> getPollsRandom() {
//        return pollService.getAllPollsRandom();
//    }
	@PostMapping
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<?> createPost(@CurrentUser UserPrincipal currentUser,
			@Valid @RequestBody PostRequest postRequest) {
		System.out.println("=====================================");
		System.out.println(postRequest.getIsUnseenOwner());
		System.out.println("=====================================");
		Post post = postService.createPost(currentUser, postRequest);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{pollId}").buildAndExpand(post.getId())
				.toUri();

		return ResponseEntity.created(location).body(new ApiResponse(true, "Poll Created Successfully"));
	}

	@PostMapping("/{pollId}/votes")
	@PreAuthorize("hasRole('USER')")
	public PostResponse castVote(@CurrentUser UserPrincipal currentUser, @PathVariable Long pollId,
			@Valid @RequestBody VoteRequest voteRequest) {
		return postService.castVoteAndGetUpdatedPost(pollId, voteRequest, currentUser);

	}
	@PostMapping("/{postId}/like")
	@PreAuthorize("hasRole('USER')")
	public PostResponse castLike(@CurrentUser UserPrincipal currentUser, @PathVariable Long postId) {
		return postService.castLikeAndGetUpdatedPost(postId, currentUser);
	}

	@PostMapping("/{pollId}/deletevotes")
	@PreAuthorize("hasRole('USER')")
	public PostResponse deleteVote(@CurrentUser UserPrincipal currentUser, @PathVariable Long pollId,
			@Valid @RequestBody VoteRequest voteRequest) {
		return postService.deleteVoted(pollId, voteRequest, currentUser);
	}

	@PutMapping("/{pollId}/endpoll")
	@PreAuthorize("hasRole('USER')")
	public PostResponse endPoll(@CurrentUser UserPrincipal currentUser, @PathVariable Long pollId) {
		return postService.endPollAndGetUpdate(pollId, currentUser);
	}

	@PostMapping("/{pollId}/deletechoice")
	@PreAuthorize("hasRole('USER')")
	public PostResponse deleteChoice(@CurrentUser UserPrincipal currentUser, @PathVariable Long pollId,
			@Valid @RequestBody VoteRequest voteRequest) {
		return postService.deleteChoice(pollId, voteRequest, currentUser);
	}

	@PostMapping("/{pollId}/addchoice")
	@PreAuthorize("hasRole('USER')")
	public PostResponse addChoice(@CurrentUser UserPrincipal currentUser, @PathVariable Long pollId,
			@Valid @RequestBody ChoiceRequest choiceRequest) {
		return postService.addChoice(pollId, choiceRequest, currentUser);
	}
}
