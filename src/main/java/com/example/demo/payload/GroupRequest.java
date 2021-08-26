package com.example.demo.payload;

import java.util.List;

public class GroupRequest {
	private String name;
	private String code;
	private List<String> usersUsername;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public List<String> getUsersUsername() {
		return usersUsername;
	}
	public void setUsersUsername(List<String> usersUsername) {
		this.usersUsername = usersUsername;
	}
	
}
