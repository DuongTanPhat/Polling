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
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.example.demo.model.audit.UserDateAudit;

@Entity
@Table(name = "groups",uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "group_code"
            })
    })
public class Group extends UserDateAudit{
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
//	@Column(columnDefinition = "nvarchar")
	private String name;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
	private User groupAdmin;
	@Column(name = "group_code")
	private String groupCode;
	@OneToMany(
            mappedBy = "group",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 30)
    private List<Participant> participants = new ArrayList<>();
	@OneToMany(
            mappedBy = "group",
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER,
            orphanRemoval = true
    )
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 30)
    private List<GroupPosting> groupPosting = new ArrayList<>();
	
	public Group() {
		super();
	}

	public List<GroupPosting> getGroupPosting() {
		return groupPosting;
	}

	public void setGroupPosting(List<GroupPosting> groupPosting) {
		this.groupPosting = groupPosting;
	}

	public String getGroupCode() {
		return groupCode;
	}

	public void setGroupCode(String groupCode) {
		this.groupCode = groupCode;
	}

	public Group(String name, User groupAdmin, String groupCode) {
		super();
		this.name = name;
		this.groupAdmin = groupAdmin;
		this.groupCode = groupCode;
	}
	public User getGroupAdmin() {
		return groupAdmin;
	}

	public void setGroupAdmin(User groupAdmin) {
		this.groupAdmin = groupAdmin;
	}

	public List<Participant> getParticipants() {
		return participants;
	}

	public void setParticipants(List<Participant> participants) {
		this.participants = participants;
	}

	public void addParticipant(Participant participant) {
		participants.add(participant);
		participant.setGroup(this);
    }

    public void removeParticipant(Participant participant) {
    	participants.remove(participant);
    	participant.setGroup(null);
    }

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
