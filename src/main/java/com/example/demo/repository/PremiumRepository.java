package com.example.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.model.Premium;

public interface PremiumRepository extends JpaRepository<Premium,Long> {
	@Query("SELECT p FROM Premium p where p.expiryDate>current_timestamp() and p.user.id=:userId")
	Premium findByUserIdPremium (Long userId);
}
