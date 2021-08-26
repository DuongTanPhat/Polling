package com.example.demo.controller;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.User;
import com.example.demo.payload.*;
import com.example.demo.repository.PollRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.StoragePostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VoteRepository;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.FileStorageService;
import com.example.demo.service.PostService;
import com.example.demo.security.CurrentUser;
import com.example.demo.util.AppConstants;

import com.example.demo.payload.ChangePasswordRequest;

import com.example.demo.exception.BadRequestException;
import com.example.demo.model.PasswordResetToken;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.PasswordTokenRepository;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PollRepository pollRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private VoteRepository voteRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private StoragePostRepository storagePostRepository;
    @Autowired
    private PostService postService;
    
    @Autowired
    private PasswordTokenRepository passwordResetTokenRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private JavaMailSender emailSender;
    
    @Autowired
	private FileStorageService fileStorageService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @PostMapping(value = "/useravatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public UploadFileResponse uploadFile(@CurrentUser UserPrincipal currentUser,@RequestParam("file") MultipartFile file) {
    	Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	System.out.println(principal);
    	System.out.println("2");
		String fileName = fileStorageService.storeFile(file,currentUser);
		System.out.println("1");
		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/getImage/").path(fileName).toUriString();
		System.out.println("1");
		return new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
	}
	@GetMapping("/getImage/{fileName:.+}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request){
		Resource resource = fileStorageService.loadFileAsResource(fileName);
		
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		}catch(IOException ex) {
			logger.info("Could not determine file type.");
		}
		if(contentType == null) {
			contentType = "application/octet-stream";
		}
		return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
	}
    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public UserSummary getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        UserSummary userSummary = new UserSummary(currentUser.getId(), currentUser.getUsername(), currentUser.getName(),currentUser.getPhoto());
        if (currentUser != null && currentUser.getAuthorities().stream()
      	      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
      	userSummary.setIsAdmin(true);
      	    }
        Integer count = notificationRepository.countByUserId(currentUser.getId());
        userSummary.setNotificationCount(count);
        return userSummary;
    }

    @GetMapping("/user/checkUsernameAvailability")
    public UserIdentityAvailability checkUsernameAvailability(@RequestParam(value = "username") String username) {
        Boolean isAvailable = !userRepository.existsByUsername(username);
        return new UserIdentityAvailability(isAvailable);
    }

    @GetMapping("/user/checkEmailAvailability")
    public UserIdentityAvailability checkEmailAvailability(@RequestParam(value = "email") String email) {
        Boolean isAvailable = !userRepository.existsByEmail(email);
        return new UserIdentityAvailability(isAvailable);
    }
    
    @GetMapping("/user/find")
    @PreAuthorize("hasRole('USER')")
    public List<UserSummary> getListUserSummary(@CurrentUser UserPrincipal currentUser,
                                                @RequestParam(value = "username", required = false) String username,
                                                @RequestParam(value = "email", required = false) String email) {
    	if(username!=null) {
    		return postService.getListUserFromUsername(username,currentUser.getId());
    	}
//    	if(email!=null) {
//    		return postService.getListUserFromEmail(email,currentUser.getId(),userlist);
//    	}
    	throw new BadRequestException("User not found");
        
    }
//    @PostMapping("/user/changepassword")
//    @PreAuthorize("hasRole('USER')")
//    public ResponseEntity<?> changepassword(@CurrentUser UserPrincipal currentUser,@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
//    	User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
//    	if(passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
//    	user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
//    	try {
//    		userRepository.save(user);
//    	}catch(Exception e) {
//    		return new ResponseEntity(new ApiResponse(false, "Change your password failed!"),
//                    HttpStatus.BAD_REQUEST);
//    	}
//    	}
//    	else return new ResponseEntity(new ApiResponse(false, "Your password is incorrect!"),
//                HttpStatus.BAD_REQUEST);
//    	return new ResponseEntity(new ApiResponse(true, "Change your password Successfully!"),
//                HttpStatus.OK);
//    }
    @GetMapping("/user/resetpassword")
    public ResponseEntity<?> resetpassword(@RequestParam(value = "email") String email) {
    	User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    	String token = UUID.randomUUID().toString();
    	PasswordResetToken myToken = new PasswordResetToken(token, user);
    	Instant now = Instant.now();
		Instant expirationDateTime = now.plus(Duration.ofMinutes(PasswordResetToken.getExpiration()));
    	myToken.setExpiryDate(expirationDateTime);
    	passwordResetTokenRepository.save(myToken);
    	SimpleMailMessage sendemail = new SimpleMailMessage();
    	sendemail.setFrom("noreply@google.com");
    	sendemail.setTo(email);
    	sendemail.setSubject("Reset Password");
//    	sendemail.setText("To complete the password reset process, please click here: "
//                + "https://testing-api-1.herokuapp.com/api/confirm-reset?token="+token+"  \n Then login with new password: "+token.split("-")[0]);
    	sendemail.setText("To complete the password reset process, please click here: "
              + "http://localhost:5000/api/confirm-reset?token="+token+"  \n Then login with new password: "+token.split("-")[0]);
    	emailSender.send(sendemail);
    	return new ResponseEntity(new ApiResponse(true, "We have sent a reset password link to your email. Please check!"),
                HttpStatus.OK);
    }
    @GetMapping("/confirm-reset")
    public ResponseEntity<?> confirmResetPassword(@RequestParam(value = "token") String token) {
    	PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
    	if(passwordResetToken==null) throw new BadRequestException("Invalid Token");
    	if(passwordResetToken.getExpiryDate().isBefore(Instant.now())){
    		throw new BadRequestException("Token expired");
    	}
    	User user = passwordResetToken.getUser();
    	user.setPassword(passwordEncoder.encode(token.split("-")[0]));
    	try {
    		userRepository.save(user);
    		passwordResetToken.setExpiryDate(Instant.now());
    		passwordResetTokenRepository.save(passwordResetToken);
    	}catch(Exception e) {
    		return new ResponseEntity(new ApiResponse(false, "Change your password failed!"),
                    HttpStatus.BAD_REQUEST);
    	}
    	return new ResponseEntity(new ApiResponse(true, "Change your password Successfully!"),
                HttpStatus.OK);
    }
    @PutMapping("/user/me")
    public ResponseEntity<?> updateName(@CurrentUser UserPrincipal currentUser,@RequestParam("name") String name){
    	if(name==null||name.trim().length()>40||name.trim().length()<4) 
    	return new ResponseEntity(new ApiResponse(false, "Change your name fail!"),
                HttpStatus.BAD_REQUEST);
    	
    	User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
    	user.setName(name.trim());
    	
    	try {
    		userRepository.save(user);
    	}catch(Exception e) {
    		return new ResponseEntity(new ApiResponse(false, "Change your name failed!"),
                    HttpStatus.BAD_REQUEST);
    	}
    	
    	return new ResponseEntity(new ApiResponse(true, "Change your name Successfully!"),
                HttpStatus.OK);
    }
//    @PostMapping("/user/updateProfile")
//    @PreAuthorize("hasRole('USER')")
//    public ResponseEntity<?> updateProfile(@CurrentUser UserPrincipal currentUser,@Valid @RequestBody UpdateProfileRequest updateProfileRequest) {
//    	User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
//    	if(passwordEncoder.matches(updateProfileRequest.getPassword(), user.getPassword())) {
////    		userRepository.updateEmail(updateProfileRequest.getEmail(), user.getId());
////    		user.setEmail(updateProfileRequest.getEmail());
////    		user.setName(updateProfileRequest.getName());
//    	try {
//    		userRepository.updateProfile(updateProfileRequest.getEmail(),updateProfileRequest.getName(), user.getId());
//    		//userRepository.save(user);
//    	}catch(Exception e) {
//    		return new ResponseEntity(new ApiResponse(false, "Change your profile failed!"),
//                    HttpStatus.BAD_REQUEST);
//    	}
//    	}
//    	else return new ResponseEntity(new ApiResponse(false, "Your password is incorrect!"),
//                HttpStatus.BAD_REQUEST);
//    	return new ResponseEntity(new ApiResponse(true, "Change your profile Successfully!"),
//                HttpStatus.OK);
//    }
    @GetMapping("/users/{username}")
    public UserProfile getUserProfile(@CurrentUser UserPrincipal currentUser,@PathVariable(value = "username") String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        long postCount = postRepository.countByCreatedBy(user.getId());
        long voteCount = voteRepository.countByUserId(user.getId());
        if(currentUser!=null&&currentUser.getUsername().equals(username)) {
        	long storageCount = storagePostRepository.countByUserId(user.getId());
        	UserProfile userProfile = new UserProfile(user.getId(), user.getUsername(), user.getName(), user.getCreatedAt(), postCount, voteCount,storageCount,user.getPhoto());
            return userProfile;
        }
        else {
        	UserProfile userProfile = new UserProfile(user.getId(), user.getUsername(), user.getName(), user.getCreatedAt(), postCount, voteCount,null,user.getPhoto());
            return userProfile;
        }
        
        
    }
    @PutMapping("/user/changepassword")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> changepassword(@CurrentUser UserPrincipal currentUser,@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
    	User user = userRepository.findById(currentUser.getId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
    	if(passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
    	user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
    	try {
    		userRepository.save(user);
    	}catch(Exception e) {
    		return new ResponseEntity(new ApiResponse(false, "Change your password failed!"),
                    HttpStatus.BAD_REQUEST);
    	}
    	}
    	else return new ResponseEntity(new ApiResponse(false, "Your password is incorrect!"),
                HttpStatus.BAD_REQUEST);
    	return new ResponseEntity(new ApiResponse(true, "Change your password Successfully!"),
                HttpStatus.OK);
    }
    @GetMapping("/users/{username}/polls")
    public PagedResponse<PostResponse> getPollsCreatedBy(@PathVariable(value = "username") String username,
                                                         @CurrentUser UserPrincipal currentUser,
                                                         @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                         @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return postService.getPostsCreatedBy(username, currentUser, page, size);
    }


    @GetMapping("/users/{username}/votes")
    public PagedResponse<PostResponse> getPollsVotedBy(@PathVariable(value = "username") String username,
                                                       @CurrentUser UserPrincipal currentUser,
                                                       @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                       @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return postService.getPostsVotedBy(username, currentUser, page, size);
    }
    @GetMapping("/users/savepost")
    public PagedResponse<PostResponse> getPollsSave(
                                                       @CurrentUser UserPrincipal currentUser,
                                                       @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                       @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return postService.getAllPollsSave(currentUser, page, size);
    }
    @GetMapping("/users/notification")
    public PagedResponse<NotificationResponse> getNotification(
                                                       @CurrentUser UserPrincipal currentUser,
                                                       @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                       @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return postService.getAllNotification(currentUser, page, size);
    }
    @PutMapping("/users/notification")
    public void readNotification(@CurrentUser UserPrincipal currentUser, @RequestParam(value = "id") Long id) {
        postService.readNotification(currentUser, id);
    }
}
