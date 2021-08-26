package com.example.demo.payload;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ChoiceRequest {
    @NotBlank
    @Size(max = 40)
    private String text;
    private String photo;
    private String review;
    

    public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getReview() {
		return review;
	}

	public void setReview(String review) {
		this.review = review;
	}

	public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
