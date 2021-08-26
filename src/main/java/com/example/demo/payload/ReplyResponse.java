package com.example.demo.payload;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReplyResponse {
	Long id;
	UserSummary user;
	String text;
	Instant createDate;
	Instant updateDate;
	Long commentId;
	
	public ReplyResponse(UserSummary user, String text) {
		super();
		this.user = user;
		this.text = text;
	}
	
	public ReplyResponse() {
		super();
	}
	

	public Long getCommentId() {
		return commentId;
	}

	public void setCommentId(Long commentId) {
		this.commentId = commentId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UserSummary getUser() {
		return user;
	}
	public void setUser(UserSummary user) {
		this.user = user;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Instant getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Instant createDate) {
		this.createDate = createDate;
	}
	public Instant getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Instant updateDate) {
		this.updateDate = updateDate;
	}
	
	
}
