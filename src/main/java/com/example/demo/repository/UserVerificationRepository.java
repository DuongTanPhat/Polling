package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.UserVerification;

public interface UserVerificationRepository extends JpaRepository<UserVerification, Long>{
	UserVerification findByVerificationCode(String VerificationCode);
}
