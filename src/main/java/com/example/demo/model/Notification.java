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
@Table(name = "notifications"
//, uniqueConstraints = {
//@UniqueConstraint(columnNames = {
//      "poll_id",
//      "user_id"
//})
//}
)
public class Notification extends DateAudit{
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;
	 @ManyToOne(fetch = FetchType.LAZY, optional = false)
	    @JoinColumn(name = "user_id", nullable = false)
	    private User user;
	 private int type;
	 private Long sourceId;
	 private boolean isRead;
//	 @Column(columnDefinition = "nvarchar")
	 private String content;
	 
	public Notification() {
		super();
	}
	public Notification(User user, int type, Long sourceId, boolean isRead, String content) {
		super();
		this.user = user;
		this.type = type;
		this.sourceId = sourceId;
		this.isRead = isRead;
		this.content = content;
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
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public Long getSourceId() {
		return sourceId;
	}
	public void setSourceId(Long sourceId) {
		this.sourceId = sourceId;
	}
	public boolean isRead() {
		return isRead;
	}
	public void setRead(boolean isRead) {
		this.isRead = isRead;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	 
	 
	 
}
