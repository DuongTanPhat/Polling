package com.example.demo.payload;

import com.example.demo.model.Vote;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

public class PollResponse {
    private Long id;
    private String question;
    private List<ChoiceResponse> choices;
    private String photo;
    private Instant creationDateTime;
    private Instant expirationDateTime;
    private Boolean isExpired;
    private Long totalVotes;
    private Long totalUserVotes;
    private Boolean isUnseenUserForVote;
    private Boolean isUnseenUserForAddChoice;
    private Boolean isAddChoice;
    private Boolean isCanFix;
    private int showResultCase;
    private int maxVotePerTimeLoad;
    private int maxVotePerChoice;
    private int maxVoteOfPoll;
    private int timeLoad;
    
	public Long getTotalUserVotes() {
		return totalUserVotes;
	}
	public void setTotalUserVotes(Long totalUserVotes) {
		this.totalUserVotes = totalUserVotes;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public List<ChoiceResponse> getChoices() {
		return choices;
	}
	public void setChoices(List<ChoiceResponse> choices) {
		this.choices = choices;
	}
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	public Instant getCreationDateTime() {
		return creationDateTime;
	}
	public void setCreationDateTime(Instant creationDateTime) {
		this.creationDateTime = creationDateTime;
	}
	public Instant getExpirationDateTime() {
		return expirationDateTime;
	}
	public void setExpirationDateTime(Instant expirationDateTime) {
		this.expirationDateTime = expirationDateTime;
	}
	
	
	public Long getTotalVotes() {
		return totalVotes;
	}
	public void setTotalVotes(Long totalVotes) {
		this.totalVotes = totalVotes;
	}
	
	public Boolean getIsExpired() {
		return isExpired;
	}
	public void setIsExpired(Boolean isExpired) {
		this.isExpired = isExpired;
	}
	public Boolean getIsUnseenUserForVote() {
		return isUnseenUserForVote;
	}
	public void setIsUnseenUserForVote(Boolean isUnseenUserForVote) {
		this.isUnseenUserForVote = isUnseenUserForVote;
	}
	public Boolean getIsUnseenUserForAddChoice() {
		return isUnseenUserForAddChoice;
	}
	public void setIsUnseenUserForAddChoice(Boolean isUnseenUserForAddChoice) {
		this.isUnseenUserForAddChoice = isUnseenUserForAddChoice;
	}
	public Boolean getIsAddChoice() {
		return isAddChoice;
	}
	public void setIsAddChoice(Boolean isAddChoice) {
		this.isAddChoice = isAddChoice;
	}
	public Boolean getIsCanFix() {
		return isCanFix;
	}
	public void setIsCanFix(Boolean isCanFix) {
		this.isCanFix = isCanFix;
	}
	public int getShowResultCase() {
		return showResultCase;
	}
	public void setShowResultCase(int showResultCase) {
		this.showResultCase = showResultCase;
	}
	public int getMaxVotePerTimeLoad() {
		return maxVotePerTimeLoad;
	}
	public void setMaxVotePerTimeLoad(int maxVotePerTimeLoad) {
		this.maxVotePerTimeLoad = maxVotePerTimeLoad;
	}
	public int getMaxVotePerChoice() {
		return maxVotePerChoice;
	}
	public void setMaxVotePerChoice(int maxVotePerChoice) {
		this.maxVotePerChoice = maxVotePerChoice;
	}
	public int getMaxVoteOfPoll() {
		return maxVoteOfPoll;
	}
	public void setMaxVoteOfPoll(int maxVoteOfPoll) {
		this.maxVoteOfPoll = maxVoteOfPoll;
	}
	public int getTimeLoad() {
		return timeLoad;
	}
	public void setTimeLoad(int timeLoad) {
		this.timeLoad = timeLoad;
	}
    
}