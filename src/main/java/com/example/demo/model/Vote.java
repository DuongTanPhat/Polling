package com.example.demo.model;

import com.example.demo.model.audit.DateAudit;
import javax.persistence.*;

@Entity
@Table(name = "votes"
//, uniqueConstraints = {
//        @UniqueConstraint(columnNames = {
//                "poll_id",
//                "user_id"
//        })
//}
)
public class Vote extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "post_id", nullable = false)
//    private Post post;
//    
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "poll_id", nullable = false)
//    private Poll poll;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "choice_id", nullable = false)
    private Choice choice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Vote() {
		super();
	}

	public Vote( Choice choice, User user) {
		super();
//		this.post = post;
//		this.poll = poll;
		this.choice = choice;
		this.user = user;
	}

//	public Post getPost() {
//		return post;
//	}
//
//	public void setPost(Post post) {
//		this.post = post;
//	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

//    public Poll getPoll() {
//        return poll;
//    }
//
//    public void setPoll(Poll poll) {
//        this.poll = poll;
//    }

    public Choice getChoice() {
        return choice;
    }

    public void setChoice(Choice choice) {
        this.choice = choice;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
