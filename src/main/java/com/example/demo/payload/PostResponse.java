package com.example.demo.payload;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public class PostResponse {
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer index;
	private Long id;
	private String question;
	private List<PollResponse> polls;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private UserSummary createdBy;
	private Long totalComment;
	private Long totalLike;
	private Boolean isUserLike;
	private Boolean isUnseenOwner;
	private Boolean isUserStorage;
	private int showCase;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Instant publicDate;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private List<GroupResponse> groupResponses= new ArrayList<GroupResponse>();
	
	public PostResponse() {
		super();
	}
	public Integer getIndex() {
		return index;
	}
	
	public Boolean getIsUserStorage() {
		return isUserStorage;
	}
	public void setIsUserStorage(Boolean isUserStorage) {
		this.isUserStorage = isUserStorage;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
	public void addGroupResponse(GroupResponse groupResponse) {
		groupResponses.add(groupResponse);
	}
	public void removeGroupResponse(GroupResponse groupResponse) {
		groupResponses.remove(groupResponse);
	}
	

	public List<GroupResponse> getGroupResponses() {
		return groupResponses;
	}
	public void setGroupResponses(List<GroupResponse> groupResponses) {
		this.groupResponses = groupResponses;
	}
	public Boolean getIsUserLike() {
		return isUserLike;
	}
	public void setIsUserLike(Boolean isUserLike) {
		this.isUserLike = isUserLike;
	}
	public Boolean getIsUnseenOwner() {
		return isUnseenOwner;
	}
	public void setIsUnseenOwner(Boolean isUnseenOwner) {
		this.isUnseenOwner = isUnseenOwner;
	}
	public int getShowCase() {
		return showCase;
	}
	public void setShowCase(int showCase) {
		this.showCase = showCase;
	}
	public Instant getPublicDate() {
		return publicDate;
	}
	public void setPublicDate(Instant publicDate) {
		this.publicDate = publicDate;
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
	public List<PollResponse> getPolls() {
		return polls;
	}
	public void setPolls(List<PollResponse> polls) {
		this.polls = polls;
	}
	public UserSummary getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(UserSummary createdBy) {
		this.createdBy = createdBy;
	}
	public Long getTotalComment() {
		return totalComment;
	}
	public void setTotalComment(Long totalComment) {
		this.totalComment = totalComment;
	}
	public Long getTotalLike() {
		return totalLike;
	}
	public void setTotalLike(Long totalLike) {
		this.totalLike = totalLike;
	}
	
}
