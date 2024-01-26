package com.bence.fileshare.controller;

import com.bence.fileshare.pojo.FolderInfo;
import com.bence.fileshare.pojo.OneFile;
import com.bence.fileshare.service.DirectoryManagerService;
import com.bence.fileshare.service.FileAccessService;
import com.bence.fileshare.utils.ZipClass;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.zip.ZipOutputStream;

//Refactor this whole class ASAP
@RestController
@RequestMapping("api/get")
@Slf4j
@CrossOrigin
public class GetController {
    private DirectoryManagerService directoryManagerService;
    private FileAccessService fileAccessService;

    public GetController(DirectoryManagerService directoryManagerService, FileAccessService fileAccessService){
        this.directoryManagerService = directoryManagerService;
        this.fileAccessService = fileAccessService;
    }

    //Neither does this
    private String setPath(String filePath){
        if(filePath.isEmpty()){
            filePath = directoryManagerService.getDataDirectory();
        }
        else{
            filePath = directoryManagerService.getDataDirectory() + "/" + filePath;
        }

        return filePath;
    }

    @GetMapping("/info")
    public ResponseEntity<FolderInfo> getFolderContents(@RequestParam(name = "path", defaultValue = "") String path) {
        try{
            return ResponseEntity.ok(fileAccessService.getInfo(path));
        }
        catch (AccessDeniedException ex){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        catch (Exception ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/oneFileInfo")
    public ResponseEntity<OneFile> getOneFileInfo(@RequestParam(name = "path", defaultValue = "") String path){
        try{
            return ResponseEntity.ok(fileAccessService.getOneFileInfo(path));
        }
        catch (AccessDeniedException ex){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        catch (Exception ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    //This need to be refactored ASAP
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam(name = "path", defaultValue = "") String path, HttpServletResponse response) {
        try {
            path = setPath(path);
            log.info("The following folder/file is being downloaded: " + path);
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
}
