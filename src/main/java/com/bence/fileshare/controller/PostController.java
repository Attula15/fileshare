package com.bence.fileshare.controller;

import com.bence.fileshare.service.FileAccessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/post")
@Slf4j
@CrossOrigin
public class PostController {
    private FileAccessService fileAccessService;

    public PostController(FileAccessService fileAccessService){
        this.fileAccessService = fileAccessService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile multipartFile,
                                             @RequestParam("isFolder") Boolean isFolder,
                                             @RequestParam("destFolder") String destFolder){
        try{
            Path path = fileAccessService.uploadFile(multipartFile, isFolder.booleanValue(), destFolder);
            if(path == null){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("The file or folder already exists");
            }
            return ResponseEntity.status(HttpStatus.OK).body("The upload was successful. The file has been uploaded to: " + path);
        }
        catch (IOException ex){
            log.error("There was an error while trying to transfer file to the server: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/createFolder")
    public ResponseEntity<String> createNewFolder(@RequestParam("destFolder") String destFolder,
                                                  @RequestParam("newFolder") String newFolderName){
        Map<String, String> resultOfFolderCreation;
        try{
            resultOfFolderCreation = fileAccessService.createFolder(destFolder, newFolderName);
        }
        catch (SecurityException secEx){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to create a folder there");
        }

        if(resultOfFolderCreation.containsKey("Failure")){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("The folder already exists! Or could not be created!");
        }
        return ResponseEntity.ok("Folder created at: " + resultOfFolderCreation.get("Success"));
    }
}
