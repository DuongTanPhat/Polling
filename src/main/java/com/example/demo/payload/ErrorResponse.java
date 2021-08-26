package com.example.demo.payload;

import java.util.Date;

public class ErrorResponse {
	private Date timestamp;
	private Integer code;
	private String error;
	private String message;
	private String path;
	
	
	
	public ErrorResponse(Date timestamp, Integer code, String error, String message, String path) {
		super();
		this.timestamp = timestamp;
		this.code = code;
		this.error = error;
		this.message = message;
		this.path = path;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
