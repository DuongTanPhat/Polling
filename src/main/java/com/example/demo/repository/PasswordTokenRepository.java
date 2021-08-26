package com.example.demo.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.PasswordResetToken;

public interface PasswordTokenRepository extends JpaRepository<PasswordResetToken, Integer>{
	PasswordResetToken findByToken(String token);
}
