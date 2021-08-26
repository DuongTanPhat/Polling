package com.example.demo.model;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
@Entity
@Table(name = "user_verifications"
)
public class UserVerification {
	private static final int EXPIRATION2 = 60 * 24 * 7;
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
	@OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;
	private String verificationCode;
	private Instant expiryDate;
	
	public UserVerification() {
		super();
	}
	
	public UserVerification(User user, String verificationCode) {
		super();
		this.user = user;
		this.verificationCode = verificationCode;
	}

	public User getUser() {
		return user;
	}
	public static int getExpiration() {
		return EXPIRATION2;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getVerificationCode() {
		return verificationCode;
	}
	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}
	public Instant getExpiryDate() {
		return expiryDate;
	}
	public void setExpiryDate(Instant expiryDate) {
		this.expiryDate = expiryDate;
	}
	
}
