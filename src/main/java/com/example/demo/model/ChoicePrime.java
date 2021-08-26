package com.example.demo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
@Entity
@Table(name = "choice_primes")
public class ChoicePrime {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	@OneToOne(mappedBy = "prime")
    private Choice choice;
	private String photo;
//	@Column(columnDefinition = "nvarchar")
	private String review;
	
	public ChoicePrime() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ChoicePrime( Choice choice, String photo, String review) {
		super();
		this.choice = choice;
		this.photo = photo;
		this.review = review;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Choice getChoice() {
		return choice;
	}
	public void setChoice(Choice choice) {
		this.choice = choice;
	}
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	public String getReview() {
		return review;
	}
	public void setReview(String review) {
		this.review = review;
	}
	
}
