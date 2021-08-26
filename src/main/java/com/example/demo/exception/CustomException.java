package com.example.demo.exception;

public class CustomException extends RuntimeException{
	 private String resourceName;
	    private String fieldName;
	    private Integer code;
	    private Object fieldValue;

	    public CustomException( String resourceName, String fieldName,Integer code, Object fieldValue) {
	        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
	        this.resourceName = resourceName;
	        this.fieldName = fieldName;
	        this.fieldValue = fieldValue;
	        this.code = code;
	    }
	    

	    public Integer getCode() {
			return code;
		}


		public String getResourceName() {
	        return resourceName;
	    }

	    public String getFieldName() {
	        return fieldName;
	    }

	    public Object getFieldValue() {
	        return fieldValue;
	    }
}
