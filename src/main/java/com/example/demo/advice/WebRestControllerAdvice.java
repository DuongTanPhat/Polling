package com.example.demo.advice;

import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.FileStorageException;
import com.example.demo.payload.ErrorResponse;


@RestControllerAdvice
public class WebRestControllerAdvice {
	
	@ExceptionHandler(CustomException.class)
	public ErrorResponse handleCustomException(CustomException ex, HttpServletResponse response, WebRequest request) {
		response.setStatus(461);
		ErrorResponse errDetail = new ErrorResponse(new Date(),ex.getCode(),"NOT FOUND", ex.getMessage(), request.getDescription(false));
        return errDetail;
	}
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ErrorResponse handleMissingParams(MissingServletRequestParameterException ex, HttpServletResponse response, WebRequest request) {
		response.setStatus(462);
		ErrorResponse errDetail = new ErrorResponse(new Date(),2,"NOT FOUND PARAMS", ex.getMessage(), request.getDescription(false));
        return errDetail;
	}
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(BadRequestException ex,HttpServletResponse response, WebRequest request) { 
    	response.setStatus(HttpStatus.BAD_REQUEST.value());
		ErrorResponse errDetail = new ErrorResponse(new Date(),3,"BAD_REQUEST", ex.getMessage(), request.getDescription(false));
        return errDetail;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException ex,HttpServletResponse response, WebRequest request) { 
    	response.setStatus(HttpStatus.NOT_FOUND.value());
		ErrorResponse errDetail = new ErrorResponse(new Date(),4,"NOT_FOUND", ex.getMessage(), request.getDescription(false));
        return errDetail;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ErrorResponse handleUnauthorizedRequest(AuthenticationException ex, HttpServletResponse response, WebRequest request) {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		ErrorResponse errDetail = new ErrorResponse(new Date(),5,"UNAUTHORIZED", ex.getMessage(), request.getDescription(false));
        return errDetail;
	}

//    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ErrorResponse handleForbiddenRequest(AccessDeniedException ex, HttpServletResponse response, WebRequest request) {
		response.setStatus(HttpStatus.FORBIDDEN.value());
		ErrorResponse errDetail = new ErrorResponse(new Date(),6,"FORBIDDEN", ex.getMessage(), request.getDescription(false));
        return errDetail;
	}
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ErrorResponse handleMaxSizeException(
      MaxUploadSizeExceededException exc, 
       HttpServletResponse response, WebRequest request) {
 
    	response.setStatus(HttpStatus.BAD_REQUEST.value());
		ErrorResponse errDetail = new ErrorResponse(new Date(),8,"BAD_REQUEST", "Max upload is 25MB", request.getDescription(false));
        return errDetail;
    }
    @ExceptionHandler(FileStorageException.class)
    public ErrorResponse handleFilenameException(
    		FileStorageException ex, 
       HttpServletResponse response, WebRequest request) {
 
    	response.setStatus(HttpStatus.BAD_REQUEST.value());
		ErrorResponse errDetail = new ErrorResponse(new Date(),9,"BAD_REQUEST",ex.getMessage(), request.getDescription(false));
        return errDetail;
    }
//    @ExceptionHandler(RuntimeException.class)
//    @ResponseStatus(value = HttpStatus.NOT_FOUND)
//    public ErrorResponse handleNotFound(RuntimeException ex, HttpServletResponse response, WebRequest request) {
//		response.setStatus(HttpStatus.NOT_FOUND.value());
//		ErrorResponse errDetail = new ErrorResponse(new Date(),7,"NOT_FOUND", ex.getMessage(), request.getDescription(false));
//        return errDetail;
//	}

//    @ExceptionHandler(ApplicationException.class)
//    @RequestMapping(value="errorPage404", method=RequestMethod.GET)
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    public String handleNotFoundRequest(ApplicationException ex,HttpServletResponse response, ModelMap map) { 
//        map.addAttribute("http-error-code", HttpStatus.NOT_FOUND);
//        return processErrorCodes(ex,response,map);
//    }
//
//
//    @ExceptionHandler(ApplicationException.class)
//    @RequestMapping(value="errorPage500", method=RequestMethod.GET)
//    @ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR,reason="Internal Server Error")
//    public String handleInternalServerError(ApplicationException ex,HttpServletResponse response, ModelMap map) { 
//        map.addAttribute("http-error-code", HttpStatus.INTERNAL_SERVER_ERROR);
//        return processErrorCodes(ex,response,map);
//    }
}
