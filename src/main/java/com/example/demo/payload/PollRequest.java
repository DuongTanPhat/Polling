package com.example.demo.payload;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.util.List;

public class PollRequest {
    @NotBlank
    @Size(max = 140)
    private String question;

    private List<ChoiceRequest> choices;
    private String photo;
  
    @Valid
    private PollLength pollLength;
    @NotNull
    private Boolean isUnseenUserForVote;
    @NotNull
    private Boolean isUnseenUserForAddChoice;
    @NotNull
    private Boolean isAddChoice;
    @NotNull
    private Boolean isCanFix;
    @NotNull
    @Positive
    private int showResultCase;
    @NotNull
    @Positive
    private int maxVotePerTimeLoad;
    @NotNull
    @Positive
    private int maxVotePerChoice;
    @NotNull
    @Positive
    private int maxVoteOfPoll;
//    @NotNull
//    @PositiveOrZero
//    private int timeLoad;
    @NotNull
    @Valid
    private PollLength timeLoad;
    
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public List<ChoiceRequest> getChoices() {
		return choices;
	}
	public void setChoices(List<ChoiceRequest> choices) {
		this.choices = choices;
	}
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	public PollLength getPollLength() {
		return pollLength;
	}
	public void setPollLength(PollLength pollLength) {
		this.pollLength = pollLength;
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
	public PollLength getTimeLoad() {
		return timeLoad;
	}
	public void setTimeLoad(PollLength timeLoad) {
		this.timeLoad = timeLoad;
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
	
    
    
}
