package com.example.demo.controller;

import com.example.demo.exception.AppException;
import com.example.demo.model.Role;
import com.example.demo.model.RoleName;
import com.example.demo.model.User;
import com.example.demo.model.UserVerification;
import com.example.demo.payload.ApiResponse;
import com.example.demo.payload.JwtAuthenticationResponse;
import com.example.demo.payload.LoginRequest;
import com.example.demo.payload.SignUpRequest;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserVerificationRepository;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.security.UserPrincipal;
import com.example.demo.exception.BadRequestException;
import com.example.demo.model.PasswordResetToken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;
    
    @Autowired
    UserVerificationRepository userVerificationRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider tokenProvider;
    
    @Autowired
    private JavaMailSender emailSender;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );
//        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
//        System.out.println(userPrincipal.isActive());
//        System.out.println(userPrincipal.isBlocked());
//        System.out.println("nope");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if(userRepository.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity(new ApiResponse(false, "Username is already taken!"),
                    HttpStatus.BAD_REQUEST);
        }

        if(userRepository.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity(new ApiResponse(false, "Email Address already in use!"),
                    HttpStatus.BAD_REQUEST);
        }

        // Creating user's account
        User user = new User(signUpRequest.getName(), signUpRequest.getUsername(),
                signUpRequest.getEmail(), signUpRequest.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new AppException("User Role not set."));

        user.setRoles(Collections.singleton(userRole));

        User result = userRepository.save(user);

        String token = UUID.randomUUID().toString();
    	UserVerification myToken = new UserVerification(user,token);
    	Instant now = Instant.now();
		Instant expirationDateTime = now.plus(Duration.ofMinutes(UserVerification.getExpiration()));
    	myToken.setExpiryDate(expirationDateTime);
    	userVerificationRepository.save(myToken);
    	SimpleMailMessage sendemail = new SimpleMailMessage();
    	sendemail.setFrom("noreply@google.com");
    	sendemail.setTo(signUpRequest.getEmail());
    	sendemail.setSubject("Confirm Password");
//    	sendemail.setText("To complete the password reset process, please click here: "
//                + "https://testing-api-1.herokuapp.com/api/active?token="+token);
    	sendemail.setText("To confirm your account process, please click here: "
              + "http://localhost:3000/auth/"+token+"  \n");
    	try {
    	emailSender.send(sendemail);
    	}
    	catch(Exception e) {
    		throw new BadRequestException("Cant send email");
    	}
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(result.getUsername()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "Waitting for active"));
    }
    @GetMapping("/active")
    public ResponseEntity<?> confirmEmail(@RequestParam(value = "token") String token) {
    	UserVerification userVerification =    userVerificationRepository.findByVerificationCode(token);
    	if(userVerification==null) throw new BadRequestException("Invalid Token");
    	if(userVerification.getExpiryDate().isBefore(Instant.now())){
    		throw new BadRequestException("Token expired");
    	}
    	User user = userVerification.getUser();
    	user.setActive(true);
    	try {
    		userRepository.save(user);
    		userVerification.setExpiryDate(Instant.now());
    		userVerificationRepository.save(userVerification);
    	}catch(Exception e) {
    		return new ResponseEntity(new ApiResponse(false, "Active your account failed!"),
                    HttpStatus.BAD_REQUEST);
    	}
    	return new ResponseEntity(new ApiResponse(true, "Active Successfully!"),
                HttpStatus.OK);
    }
}