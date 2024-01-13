package com.bence.fileshare.controller;

import com.bence.fileshare.service.FileAccessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/delete")
@Slf4j
@CrossOrigin
public class DeleteController {
    private FileAccessService fileAccessService;

    public DeleteController(FileAccessService fileAccessService){
        this.fileAccessService = fileAccessService;
    }

    @DeleteMapping()
    public ResponseEntity<String> delete(@RequestParam(name = "filePath", defaultValue = "") String filePath) throws SecurityException{
        Map<String, String> result = fileAccessService.delete(filePath);

        if(result.containsKey("Failure")){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result.get("Failure"));
        }
        return ResponseEntity.ok().body(result.get("Success"));
    }
}
