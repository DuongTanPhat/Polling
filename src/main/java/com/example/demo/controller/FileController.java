package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.FileStorageException;
import com.example.demo.model.User;
import com.example.demo.payload.UploadFileResponse;
import com.example.demo.repository.PollRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VoteRepository;
import com.example.demo.security.CurrentUser;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.FileStorageService;
import com.example.demo.service.GoogleDriveService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@CrossOrigin("*")
@RestController
@RequestMapping("/api/file")
public class FileController {
	
	private static final Logger logger = LoggerFactory.getLogger(FileController.class);
	
	@Autowired
    private PollRepository pollRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private UserRepository userRepository;
	@Autowired
	private FileStorageService fileStorageService;
	@Autowired
	private GoogleDriveService googleService;
	@PostMapping(value = "/useravatar")
	public UploadFileResponse uploadFileAva(@CurrentUser UserPrincipal currentUser,@RequestParam("file") MultipartFile file) {
		
		
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		if (fileName.contains("..")) {
			throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);

		}
		String fileExtension = "";
		fileExtension = fileName.substring(fileName.lastIndexOf("."));
		System.out.println(fileExtension);
		if (!(fileExtension.contains("png") || fileExtension.contains("jpg") || fileExtension.contains("jpeg"))) {
			System.out.println(fileExtension);
			throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
		}
		try {

			
			UploadFileResponse u = googleService.upload(file);
			if(currentUser.getPhoto()!=null&&!currentUser.getPhoto().equals(""))
			googleService.delete(currentUser.getPhoto());
			User user = userRepository.getOne(currentUser.getId());
			user.setPhoto(u.getId());
			userRepository.saveAndFlush(user);
			return u;
		} catch (IOException ex) {
			throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
		}
		
	}
//	@PostMapping(value = "/upload")
//	public UploadFileResponse uploadFilePost(@CurrentUser UserPrincipal currentUser,@RequestParam("file") MultipartFile file) {
//		String fileName = fileStorageService.storeFile(file,currentUser);
//		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/file/getImage/").path(fileName).toUriString();
//		return new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
//	}
	@PostMapping(value = "/upload")
	public UploadFileResponse uploadFilePost(@RequestParam("file") MultipartFile file) {
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());

		if (fileName.contains("..")) {
			throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);

		}
		String fileExtension = "";
		fileExtension = fileName.substring(fileName.lastIndexOf("."));
		System.out.println(fileExtension);
		if (!(fileExtension.contains("png") || fileExtension.contains("jpg") || fileExtension.contains("jpeg"))) {
			System.out.println(fileExtension);
			throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
		}
		try {
			return  googleService.upload(file);
		}catch (IOException e) {
			throw new BadRequestException("Error");
			// TODO: handle exception
		}
		
	}
	@DeleteMapping(value = "/upload")
	public void deleteFilePost(@RequestParam("id") String id) {
		try {
			googleService.delete(id);
		}catch (IOException e) {
			throw new BadRequestException("Error");
			// TODO: handle exception
		}
		
	}
	@GetMapping("/getImageAva/{fileName:.+}")
	public ResponseEntity<Resource> downloadFileAva(@PathVariable String fileName, HttpServletRequest request){
		String name = "Ava" +File.separatorChar + fileName;
		Resource resource = fileStorageService.loadFileAsResource(name);
		
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		}catch(IOException ex) {
			logger.info("Could not determine file type.");
		}
		if(contentType == null) {
			contentType = "application/octet-stream";
		}
		return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
	}
	@GetMapping("/getImage/{fileName:.+}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request){
		//String name = "Ava" +File.separatorChar + fileName;
		Resource resource = fileStorageService.loadFileAsResource(fileName);
		
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		}catch(IOException ex) {
			logger.info("Could not determine file type.");
		}
		if(contentType == null) {
			contentType = "application/octet-stream";
		}
		return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
	}
//	@GetMapping("/getAudio/{fileName:.+}")
//	public ResponseEntity<Resource> downloadFileAudio(@PathVariable String fileName, HttpServletRequest request){
//		String name = "audio" + File.separatorChar + fileName;
//		Resource resource = fileStorageService.loadFileAsResource(name);
//		
//		String contentType = null;
//		try {
//			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
//		}catch(IOException ex) {
//			logger.info("Could not determine file type.");
//		}
//		if(contentType == null) {
//			contentType = "audio/mpeg";
//		}
//		return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType(contentType))
//                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
//                .body(resource);
//	}
}
