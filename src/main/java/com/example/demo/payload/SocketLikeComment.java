package com.example.demo.payload;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SocketLikeComment {
	private Integer type;
	private Long postId;
	private Long totalLike;
	private Long totalComment;
	private CommentResponse comment;
	private ReplyResponse reply;
	
	public CommentResponse getComment() {
		return comment;
	}
	public void setComment(CommentResponse comment) {
		this.comment = comment;
	}
	public ReplyResponse getReply() {
		return reply;
	}
	public void setReply(ReplyResponse reply) {
		this.reply = reply;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Long getPostId() {
		return postId;
	}
	public void setPostId(Long postId) {
		this.postId = postId;
	}
	public Long getTotalLike() {
		return totalLike;
	}
	public void setTotalLike(Long totalLike) {
		this.totalLike = totalLike;
	}
	public Long getTotalComment() {
		return totalComment;
	}
	public void setTotalComment(Long totalComment) {
		this.totalComment = totalComment;
	}
	
}
