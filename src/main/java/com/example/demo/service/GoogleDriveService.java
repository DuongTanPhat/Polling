package com.example.demo.service;


import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.payload.UploadFileResponse;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

@Service
public class GoogleDriveService {
	@Autowired
    private Drive googleDrive;
	public UploadFileResponse upload(MultipartFile multipartFile) throws IOException {
		File newGGDriveFile = new File();
		List<String> parents = new ArrayList<String>();
		parents.add("1vygtYrp59pIhIduE8aPDGF5MMBhnLsoZ");
        newGGDriveFile.setParents(parents).setName(multipartFile.getOriginalFilename());
        java.io.File file = new java.io.File(multipartFile.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(multipartFile.getBytes());
        fos.close();
        FileContent mediaContent = new FileContent("image/jpeg", file);
        File file2 = googleDrive.files().create(newGGDriveFile, mediaContent).setFields("id,webContentLink,name,mimeType,webViewLink,size").execute();
        System.out.println(file2.getWebContentLink());
        System.out.println(file2.getId());
        UploadFileResponse upload = new UploadFileResponse(file2.getName(),file2.getWebContentLink(),file2.getMimeType(),file2.getSize());
        upload.setId(file2.getId());
        return upload;
	}
	public void delete(String fileId) throws IOException {
		
		try {
		      googleDrive.files().delete(fileId).execute();
		    } catch (IOException e) {
		      System.out.println("An error occurred: " + e);
		    }
	}
}

