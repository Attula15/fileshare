package com.bence.fileshare.controller;

import com.bence.fileshare.pojo.FolderInfo;
import com.bence.fileshare.service.FileAccessService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.AccessDeniedException;
import org.springframework.core.io.Resource;

@RestController
@RequestMapping("api")
@Slf4j
public class MainController {

    @Autowired
    private FileAccessService fileAccessService;

    @GetMapping("/getInfo")
    public ResponseEntity<FolderInfo> getFolderContents(@RequestParam("path") String path) {
        try{
            return ResponseEntity.ok(fileAccessService.getInfo(path));
        }
        catch (AccessDeniedException ex){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/downloadFile")
    public ResponseEntity<Object> downloadFile(@RequestParam("path") String path) throws IOException {
        try {
            Object object = fileAccessService.downloadFile(path);
            File file = new File(path);
            if(file.isFile()){
                Resource resource = (Resource) object;

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            }
            else{
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=download.zip")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(object);
            }
        }
        catch (Exception ex){
            return ResponseEntity.notFound().build();
        }
    }
}
