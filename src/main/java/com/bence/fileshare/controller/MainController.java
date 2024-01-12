package com.bence.fileshare.controller;

import com.bence.fileshare.pojo.FolderInfo;
import com.bence.fileshare.pojo.OneFile;
import com.bence.fileshare.pojo.SimpleString;
import com.bence.fileshare.service.FileAccessService;
import com.bence.fileshare.utils.ZipClass;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api")
@Slf4j
@CrossOrigin
public class MainController {
    private FileAccessService fileAccessService;

    public MainController(FileAccessService fileAccessService){
        this.fileAccessService = fileAccessService;
    }

    @GetMapping("/getInfo")
    public ResponseEntity<FolderInfo> getFolderContents(@RequestParam("path") String path) {
        try{
            return ResponseEntity.ok(fileAccessService.getInfo(path));
        }
        catch (AccessDeniedException ex){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/getOneFileInfo")
    public ResponseEntity<OneFile> getOneFileInfo(@RequestParam("path") String path){
        try{
            return ResponseEntity.ok(fileAccessService.getOneFileInfo(path));
        }
        catch (AccessDeniedException ex){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/basePath")
    public ResponseEntity<SimpleString> getBasePath(){
        return ResponseEntity.ok(fileAccessService.getRootDirectory());
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("path") String path, HttpServletResponse response) {
        log.info("The following folder/file is being downloaded: " + path);
        try {
            File file = new File(path);
            if(file.isFile()){
                Resource resource = fileAccessService.downloadFile(path);

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            }
            else{
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=\"downloaded.zip\"");

                ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());

                ZipClass.addFolderToZip("", file.toPath(), zipOutputStream);

                zipOutputStream.close();

                return ResponseEntity.ok().build();
            }
        }
        catch (AccessDeniedException ex){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        catch (IOException ex){
            log.error("IOExcepiton has been thrown: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
