package com.example.demo.payload;

import java.time.Instant;

import com.example.demo.model.Post;

public class ExPost {
	private Post post;
	private Instant max;
	
	public ExPost(Post post, Instant max) {
		super();
		this.post = post;
		this.max = max;
	}
	public Long getId() {
		return post.getId();
	}
	public Post getPost() {
		return post;
	}
	public void setPost(Post post) {
		this.post = post;
	}
	public Instant getMax() {
		return max;
	}
	public void setMax(Instant max) {
		this.max = max;
	}
	
	
	
}
