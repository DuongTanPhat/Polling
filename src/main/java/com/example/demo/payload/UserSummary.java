package com.example.demo.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSummary {
    private Long id;
    private String username;
    private String name;
    private String photo;
    private Boolean isAdmin;
    private Integer notificationCount;
    public UserSummary(Long id, String username, String name,String photo) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.photo = photo;
    }
    

	public Integer getNotificationCount() {
		return notificationCount;
	}


	public void setNotificationCount(Integer notificationCount) {
		this.notificationCount = notificationCount;
	}


	public Boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}