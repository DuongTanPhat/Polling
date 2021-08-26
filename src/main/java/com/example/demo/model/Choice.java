package com.example.demo.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.example.demo.model.audit.CreateAudit;

import java.util.Objects;

@Entity
@Table(name = "choices")
public class Choice extends CreateAudit{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 40)
//    @Column(columnDefinition = "nvarchar")
    private String text;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "choice_prime_id", nullable=true)
    private ChoicePrime prime;
    

    public Choice(@NotBlank @Size(max = 40) String text, Poll poll, ChoicePrime prime) {
		super();
		this.text = text;
		this.poll = poll;
		this.prime = prime;
	}


	public Choice() {

    }
    

    public ChoicePrime getPrime() {
		return prime;
	}


	public void setPrime(ChoicePrime prime) {
		this.prime = prime;
	}


	public Choice(String text) {
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Choice choice = (Choice) o;
        return Objects.equals(id, choice.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
