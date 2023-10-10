package com.bence.fileshare.controller;

import com.bence.fileshare.pojo.FolderInfo;
import com.bence.fileshare.service.FileAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("api")
public class MainController {

    @Autowired
    private FileAccessService fileAccessService;

    @GetMapping("/getInfo")
    public FolderInfo getFolderContents(@RequestParam("path") String path) throws AccessDeniedException {
        return fileAccessService.getInfo(path);
    }
}
