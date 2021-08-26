package com.example.demo.payload;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SocketChoiceResponse {
	private Integer type;
	private List<ChoiceResponse> choices=new ArrayList<ChoiceResponse>();
	private Long postId;
	private Long pollId;
	private Long totalVotes;
	
	public Long getTotalVotes() {
		return totalVotes;
	}
	public void setTotalVotes(Long totalVotes) {
		this.totalVotes = totalVotes;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public void addChoiceResponse(ChoiceResponse choiceResponse) {
		choices.add(choiceResponse);
	}
	public List<ChoiceResponse> getChoices() {
		return choices;
	}
	public void setChoices(List<ChoiceResponse> choices) {
		this.choices = choices;
	}
	public Long getPostId() {
		return postId;
	}
	public void setPostId(Long postId) {
		this.postId = postId;
	}
	public Long getPollId() {
		return pollId;
	}
	public void setPollId(Long pollId) {
		this.pollId = pollId;
	}
	
}
