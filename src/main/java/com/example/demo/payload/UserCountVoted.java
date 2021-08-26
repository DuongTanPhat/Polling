package com.example.demo.payload;

import com.example.demo.model.User;

public class UserCountVoted {
	private Long id;
    private String username;
    private String name;
    private String photo;
    private Long count;
    public UserCountVoted(User user,Long count) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.photo = user.getPhoto();
        this.count = count;
    }
    
    
   


	public Long getCount() {
		return count;
	}





	public void setCount(Long count) {
		this.count = count;
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
