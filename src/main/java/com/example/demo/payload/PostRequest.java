package com.example.demo.payload;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public class PostRequest {
	private String question;
	private List<PollRequest> polls;
	private Boolean isUnseenOwner;
	private int showCase;
	private String publicDate;
	private List<String> groups;
	private List<String> usersEmail;
	private List<String> usersUsername;
	
	public int getShowCase() {
		return showCase;
	}
	public void setShowCase(int showCase) {
		this.showCase = showCase;
	}
	
	public List<String> getGroups() {
		return groups;
	}
	public void setGroups(List<String> groups) {
		this.groups = groups;
	}
	
	public List<String> getUsersEmail() {
		return usersEmail;
	}
	public void setUsersEmail(List<String> usersEmail) {
		this.usersEmail = usersEmail;
	}
	public List<String> getUsersUsername() {
		return usersUsername;
	}
	public void setUsersUsername(List<String> usersUsername) {
		this.usersUsername = usersUsername;
	}
	public String getPublicDate() {
		return publicDate;
	}
	public void setPublicDate(String publicDate) {
		this.publicDate = publicDate;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public List<PollRequest> getPolls() {
		return polls;
	}
	public void setPolls(List<PollRequest> polls) {
		this.polls = polls;
	}
	public Boolean getIsUnseenOwner() {
		return isUnseenOwner;
	}
	public void setIsUnseenOwner(Boolean isUnseenOwner) {
		this.isUnseenOwner = isUnseenOwner;
	}
	
}
