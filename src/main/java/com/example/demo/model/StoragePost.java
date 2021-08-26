package com.example.demo.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.example.demo.model.audit.DateAudit;

@Entity
@Table(name = "storage_posts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "user_id",
                "post_id"
        })}
)
public class StoragePost extends DateAudit{
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	 @ManyToOne(fetch = FetchType.LAZY, optional = false)
	    @JoinColumn(name = "user_id", nullable = false)
	    private User user;
	 @ManyToOne(fetch = FetchType.LAZY, optional = false)
	    @JoinColumn(name = "post_id", nullable = false)
	    private Post post;
	 
	public StoragePost() {
		super();
	}
	public StoragePost(User user, Post post) {
		super();
		this.user = user;
		this.post = post;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
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
	 
}
