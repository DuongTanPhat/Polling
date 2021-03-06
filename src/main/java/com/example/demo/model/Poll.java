package com.example.demo.model;

import com.example.demo.model.audit.DateAudit;
import com.example.demo.model.audit.UserDateAudit;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "polls")
public class Poll extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    
    @NotBlank
//    @Column(columnDefinition = "nvarchar")
    private String question;

    @OneToMany(
            mappedBy = "poll",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 30)
    private List<Choice> choices = new ArrayList<>();
    private String photo;
//    @NotNull
    private Instant expirationDateTime;

    private boolean isUnseenUserForVote;

    private boolean isUnseenUserForAddChoice;

    private boolean isAddChoice;

    private boolean isCanFix;


    @PositiveOrZero
    private int showResultCase;

    
    private int maxVotePerTimeLoad;

    private int maxVotePerChoice;
    
    private int maxVoteOfPoll;
    
    private int timeLoad;
    
    public Post getPost() {
		return post;
	}
    
	public void setPost(Post post) {
		this.post = post;
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

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public boolean isUnseenUserForVote() {
		return isUnseenUserForVote;
	}

	public void setUnseenUserForVote(boolean isUnseenUserForVote) {
		this.isUnseenUserForVote = isUnseenUserForVote;
	}

	public boolean isUnseenUserForAddChoice() {
		return isUnseenUserForAddChoice;
	}

	public void setUnseenUserForAddChoice(boolean isUnseenUserForAddChoice) {
		if(isUnseenUserForAddChoice==true) {
			this.isAddChoice = true;
		}
		this.isUnseenUserForAddChoice = isUnseenUserForAddChoice;
	}

	public int getShowResultCase() {
		return showResultCase;
	}

	public void setShowResultCase(int showResultCase) {
		this.showResultCase = showResultCase;
	}


	public boolean isAddChoice() {
		return isAddChoice;
	}

	public void setAddChoice(boolean isAddChoice) {
		this.isAddChoice = isAddChoice;
	}

	public boolean isCanFix() {
		return isCanFix;
	}

	public void setCanFix(boolean isCanFix) {
		this.isCanFix = isCanFix;
	}

	public int getTimeLoad() {
		return timeLoad;
	}

	public void setTimeLoad(int timeLoad) {
		this.timeLoad = timeLoad;
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

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public Instant getExpirationDateTime() {
        return expirationDateTime;
    }

    public void setExpirationDateTime(Instant expirationDateTime) {
        this.expirationDateTime = expirationDateTime;
    }

    public void addChoice(Choice choice) {
        choices.add(choice);
        choice.setPoll(this);
    }

    public void removeChoice(Choice choice) {
        choices.remove(choice);
        choice.setPoll(null);
    }
}
