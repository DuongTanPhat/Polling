package com.example.demo.payload;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentResponse {
	Long id;
	UserSummary user;
	String text;
	List<ReplyResponse> replys;
	Instant createDate;
	Instant updateDate;
	
	public CommentResponse() {
		super();
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
	public List<ReplyResponse> getReplys() {
		return replys;
	}
	public void setReplys(List<ReplyResponse> replys) {
		this.replys = replys;
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
