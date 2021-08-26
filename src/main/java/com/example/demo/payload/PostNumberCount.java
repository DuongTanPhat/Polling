package com.example.demo.payload;

public class PostNumberCount {
	 private Long postId;
	 private Long count;
	 
	public PostNumberCount(Long postId, Long count) {
		super();
		this.postId = postId;
		this.count = count;
	}
	public Long getPostId() {
		return postId;
	}
	public void setPostId(Long postId) {
		this.postId = postId;
	}
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
	 
}
