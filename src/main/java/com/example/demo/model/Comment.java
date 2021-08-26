package com.example.demo.model;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.example.demo.model.audit.DateAudit;

@Entity
@Table(name = "comments"
)
public class Comment extends DateAudit{
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	 @ManyToOne(fetch = FetchType.LAZY, optional = false)
	    @JoinColumn(name = "user_id", nullable = false)
	    private User user;
	 @ManyToOne(fetch = FetchType.LAZY, optional = false)
	    @JoinColumn(name = "post_id", nullable = false)
	    private Post post;
//	 @Column(columnDefinition = "nvarchar")
	 private String text;
	 @OneToMany(
	            mappedBy = "comment",
	            cascade = CascadeType.ALL,
	            fetch = FetchType.EAGER,
	            orphanRemoval = true
	    )
	    @Fetch(FetchMode.SELECT)
	    @BatchSize(size = 30)
	    private List<Reply> replys = new ArrayList<>();
	 
	public Comment() {
		super();
	}
	public Comment(User user, Post post, String text) {
		super();
		this.user = user;
		this.post = post;
		this.text = text;
	}
	  public void addReply(Reply reply) {
	        replys.add(reply);
	        reply.setComment(this);
	    }

	    public void removeReply(Reply reply) {
	        replys.remove(reply);
	        reply.setComment(null);
	    }
	public List<Reply> getReplys() {
		return replys;
	}
	public void setReplys(List<Reply> replys) {
		this.replys = replys;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public Post getPost() {
		return post;
	}
	public void setPost(Post post) {
		this.post = post;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	 
}
