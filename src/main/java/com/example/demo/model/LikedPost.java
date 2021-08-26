package com.example.demo.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
@Entity
@Table(name = "liked_posts"
)
public class LikedPost {
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
	 @ManyToOne(fetch = FetchType.LAZY, optional = false)
	    @JoinColumn(name = "user_id", nullable = false)
	    private User user;
	 @ManyToOne(fetch = FetchType.LAZY, optional = false)
	    @JoinColumn(name = "post_id", nullable = false)
	    private Post post;
	 private Boolean isLike;
	 
	public LikedPost() {
		super();
	}
	public LikedPost(User user, Post post, boolean isLike) {
		super();
		this.user = user;
		this.post = post;
		this.isLike = isLike;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Post getPost() {
		return post;
	}
	public void setPost(Post post) {
		this.post = post;
	}
	public Boolean getIsLike() {
		return isLike;
	}
	public void setIsLike(Boolean isLike) {
		this.isLike = isLike;
	}
	
	 
}
