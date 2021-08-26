package com.example.demo.payload;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChoiceResponse {
    private long id;
    private String text;
    private long voteCount;

    private UserSummary createdBy;
    private Instant creationDateTime;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int userVoteCount;
    private ChoicePrimeResponse choicePrime;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    private List<UserSummary> usersVoted;
//    
//
//	public List<UserSummary> getUsersVoted() {
//		return usersVoted;
//	}
//
//	public void setUsersVoted(List<UserSummary> usersVoted) {
//		this.usersVoted = usersVoted;
//	}

	public UserSummary getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(UserSummary createdBy) {
		this.createdBy = createdBy;
	}

	public Instant getCreationDateTime() {
		return creationDateTime;
	}

	public void setCreationDateTime(Instant creationDateTime) {
		this.creationDateTime = creationDateTime;
	}

	public int getUserVoteCount() {
		return userVoteCount;
	}

	public void setUserVoteCount(int userVoteCount) {
		this.userVoteCount = userVoteCount;
	}

	public ChoicePrimeResponse getChoicePrime() {
		return choicePrime;
	}

	public void setChoicePrime(ChoicePrimeResponse choicePrime) {
		this.choicePrime = choicePrime;
	}

	public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(long voteCount) {
        this.voteCount = voteCount;
    }
}
