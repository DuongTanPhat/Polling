package com.example.demo.payload;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public class GroupResponse {
	 private Long id;
	private String name;
	private String code;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private UserSummary admin;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Instant createDate;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String usernameAdmin;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private List<UserSummary> member= new ArrayList<UserSummary>();;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	public GroupResponse(Long id, String name, String code) {
		super();
		this.id = id;
		this.name = name;
		this.code = code;
	}
	
	
	public void addMember(UserSummary u) {
		this.member.add(u);
	}
	public List<UserSummary> getMember() {
		return member;
	}
	public void setMember(List<UserSummary> member) {
		this.member = member;
	}
	public GroupResponse(Long id, String name, String code, Instant createDate, String usernameAdmin) {
		super();
		this.id = id;
		this.name = name;
		this.code = code;
		this.createDate = createDate;
		this.usernameAdmin = usernameAdmin;
	}
	public String getUsernameAdmin() {
		return usernameAdmin;
	}
	public void setUsernameAdmin(String usernameAdmin) {
		this.usernameAdmin = usernameAdmin;
	}
	public UserSummary getAdmin() {
		return admin;
	}
	public void setAdmin(UserSummary admin) {
		this.admin = admin;
	}
	public Instant getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Instant createDate) {
		this.createDate = createDate;
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
