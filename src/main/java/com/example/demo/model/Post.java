package com.example.demo.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.example.demo.model.audit.UserDateAudit;

@Entity
@Table(name = "posts"
)
public class Post extends UserDateAudit{
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;
//	 @Column(columnDefinition = "nvarchar")
	    private String question;
	    @OneToMany(
	            mappedBy = "post",
	            cascade = CascadeType.ALL,
	            fetch = FetchType.EAGER,
	            orphanRemoval = true
	    )
	    @Size(min = 1)
	    @Fetch(FetchMode.SELECT)
	    @BatchSize(size = 30)
	    private List<Poll> polls = new ArrayList<>();
	    @OneToMany(
	            mappedBy = "post",
	            cascade = CascadeType.ALL,
	            fetch = FetchType.EAGER,
	            orphanRemoval = true
	    )
	    @Fetch(FetchMode.SELECT)
	    @BatchSize(size = 30)
	    private List<Comment> comments = new ArrayList<>();
	    @OneToMany(
	            mappedBy = "post",
	            cascade = CascadeType.ALL,
	            fetch = FetchType.EAGER,
	            orphanRemoval = true
	    )
	    @Fetch(FetchMode.SELECT)
	    @BatchSize(size = 30)
	    private List<LikedPost> likeds = new ArrayList<>();
	    
	    @OneToMany(
	            mappedBy = "post",
	            cascade = CascadeType.ALL,
	            fetch = FetchType.EAGER,
	            orphanRemoval = true
	    )
	    @Fetch(FetchMode.SELECT)
	    @BatchSize(size = 30)
	    private List<Relationship> relationships = new ArrayList<>();
	    
	    @OneToMany(
	            mappedBy = "post",
	            cascade = CascadeType.ALL,
	            fetch = FetchType.EAGER,
	            orphanRemoval = true
	    )
	    @Fetch(FetchMode.SELECT)
	    @BatchSize(size = 30)
	    private List<GroupPosting> groupPostings = new ArrayList<>();
	    
	    @PositiveOrZero
	    private int showCase;
	    
	    private Instant publicDate;
	    private boolean isUnseenOwner;
	    
		public Post() {
			super();
		}

		
		
		public Post(String question, @Size(min = 1) List<Poll> polls, List<Comment> comments, List<LikedPost> likeds,
				@PositiveOrZero int showCase, Instant publicDate) {
			super();
			this.question = question;
			this.polls = polls;
			this.comments = comments;
			this.likeds = likeds;
			this.showCase = showCase;
			this.publicDate = publicDate;
		}
		


		public boolean isUnseenOwner() {
			return isUnseenOwner;
		}



		public void setUnseenOwner(boolean isUnseenOwner) {
			this.isUnseenOwner = isUnseenOwner;
		}



		public List<GroupPosting> getGroupPostings() {
			return groupPostings;
		}



		public void setGroupPostings(List<GroupPosting> groupPostings) {
			this.groupPostings = groupPostings;
		}



		public List<Relationship> getRelationships() {
			return relationships;
		}



		public void setRelationships(List<Relationship> relationships) {
			this.relationships = relationships;
		}



		public List<LikedPost> getLikeds() {
			return likeds;
		}

		public void setLikeds(List<LikedPost> likeds) {
			this.likeds = likeds;
		}

		public void addPoll(Poll poll) {
	        polls.add(poll);
	        poll.setPost(this);
	    }

	    public void removePoll(Poll poll) {
	        polls.remove(poll);
	        poll.setPost(null);
	    }
	    public void addGroupPosting(GroupPosting groupPosting) {
	    	groupPostings.add(groupPosting);
	    	groupPosting.setPost(this);
	    }

	    public void removeGroupPosting(GroupPosting groupPosting) {
	    	groupPostings.remove(groupPosting);
	    	groupPosting.setPost(null);
	    }
	    
	    public void addRelationship(Relationship relationship) {
	    	relationships.add(relationship);
	    	relationship.setPost(this);
	    }

	    public void removeRelationship(Relationship relationship) {
	    	relationships.remove(relationship);
	    	relationship.setPost(null);
	    }
	    
	    public void addComment(Comment comment) {
	        comments.add(comment);
	        comment.setPost(this);
	    }

	    public void removeComment(Comment comment) {
	    	comments.remove(comment);
	    	comment.setPost(null);
	    }
	    public void addLike(LikedPost like) {
	    	likeds.add(like);
	    	like.setPost(this);
	    }

	    public void removeLike(LikedPost like) {
	    	likeds.remove(like);
	    	like.setPost(null);
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

		public List<Poll> getPolls() {
			return polls;
		}

		public void setPolls(List<Poll> polls) {
			this.polls = polls;
		}

		public List<Comment> getComments() {
			return comments;
		}

		public void setComments(List<Comment> comments) {
			this.comments = comments;
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
	    
}
