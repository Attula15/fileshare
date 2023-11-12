package com.bence.fileshare.service;

import com.bence.fileshare.pojo.FolderInfo;
import com.bence.fileshare.pojo.OneFile;
import com.bence.fileshare.pojo.SimpleString;
import com.bence.fileshare.utils.FileSizeConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FileAccessService {
    @Value("${my_root_directory}")
    private String rootDirectory;

    private final String tempRootDirectory = "C:\\testfolder\\testFolder2";

    public SimpleString getRootDirectory(){
        return new SimpleString(rootDirectory);
    }

    public FolderInfo getInfo(String folderPath) throws AccessDeniedException {
        if(rootDirectory.equals("none") || rootDirectory.isEmpty()){
            log.warn("The root directory has not been set.");
        }
        log.info(rootDirectory);

        File folder = new File(folderPath);
        FolderInfo returnable = new FolderInfo();

        if(!folder.canRead()){
            log.error("Don't have permission to read the given folder: " + folderPath);
            throw new AccessDeniedException("Don't have permission to read the given folder: " + folderPath);
        }
        else if(!folder.canWrite()){
            log.error("Don't have permission to write into the given folder: " + folderPath);
            throw new AccessDeniedException("Don't have permission to write into the given folder: " + folderPath);
        }

        if(!folder.isDirectory()){
            returnable.setFilesInDir(null);
            returnable.setFolderSize(FileSizeConverter.convert(FileUtils.sizeOf(folder)));
            return returnable;
        }

        File[] dirListing = folder.listFiles();
        returnable.setFilesInDir(null);

        List<OneFile> list = new ArrayList<>();

        if(dirListing != null){
            for(File file : dirListing){
                list.add(new OneFile(file.isDirectory(), file.getName()));
            }
            returnable.setFilesInDir(list);
        }

        returnable.setFolderSize(FileSizeConverter.convert(FileUtils.sizeOfDirectory(folder)));

        return returnable;
    }

    public Resource downloadFile(String filePath) throws IOException {
        File file = new File(filePath);

        if(file.exists()){
            if(file.canRead()){
                return new UrlResource(file.toURI());
            }
            else{
                log.error("Cannot access file: " + filePath);
                throw new AccessDeniedException("Cannot access file: " + filePath);
            }
        }
        else{
            log.error("The file does not exists: " + filePath);
            throw new FileExistsException("The file does not exists: " + filePath);
        }
    }

    public Path uploadFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(rootDirectory, file.getOriginalFilename());
        file.transferTo(uploadPath);

        return uploadPath;
    }
}
