package com.example.demo.payload;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {
	private Long id;
	private int type;
	private Long sourceId;
	private boolean isRead;
	private String content;
	private Instant createAt;
	
	public NotificationResponse(Long id, int type, Long sourceId, boolean isRead, String content,Instant createAt) {
		super();
		this.id = id;
		this.type = type;
		this.sourceId = sourceId;
		this.isRead = isRead;
		this.content = content;
		this.createAt = createAt;
	}
	
	public Instant getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Instant createAt) {
		this.createAt = createAt;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
