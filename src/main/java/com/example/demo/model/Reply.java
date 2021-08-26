package com.example.demo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.example.demo.model.audit.DateAudit;

@Entity
@Table(name = "replys"
)
public class Reply extends DateAudit{
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	 @ManyToOne(fetch = FetchType.LAZY, optional = false)
	    @JoinColumn(name = "user_id", nullable = false)
	    private User user;
	 @ManyToOne(fetch = FetchType.LAZY, optional = false)
	    @JoinColumn(name = "comment_id", nullable = false)
	    private Comment comment;
//	 @Column(columnDefinition = "nvarchar")
	 private String text;
	 
	public Reply() {
		super();
	}
	public Reply(User user, Comment comment, String text) {
		super();
		this.user = user;
		this.comment = comment;
		this.text = text;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Comment getComment() {
		return comment;
	}
	public void setComment(Comment comment) {
		this.comment = comment;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
}
