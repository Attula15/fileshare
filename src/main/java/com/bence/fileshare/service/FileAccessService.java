package com.bence.fileshare.service;

import com.bence.fileshare.pojo.FolderInfo;
import com.bence.fileshare.pojo.OneFile;
import com.bence.fileshare.pojo.SimpleString;
import com.bence.fileshare.utils.FileSizeConverter;
import com.bence.fileshare.utils.ZipClass;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class FileAccessService {
    @Value("${my_root_directory}")
    private String rootDirectory;

    public SimpleString getRootDirectory(){
        return new SimpleString(rootDirectory);
    }

    private String setPath(String filePath){
        if(filePath.isEmpty()){
            filePath = rootDirectory;
        }
        else{
            filePath = rootDirectory + "/" + filePath;
        }

        return filePath;
    }

    public FolderInfo getInfo(String folderPath) throws AccessDeniedException {
        if(rootDirectory.equals("none") || rootDirectory.isEmpty()){
            log.warn("The root directory has not been set.");
        }
        log.info(rootDirectory);

        folderPath = setPath(folderPath);

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

    public OneFile getOneFileInfo(String filePath) throws AccessDeniedException{
        filePath = setPath(filePath);
        File file = new File(filePath);

        if(!file.canRead()){
            log.error("Don't have permission to read the given folder: " + filePath);
            throw new AccessDeniedException("Don't have permission to read the given folder: " + filePath);
        }
        else if(!file.canWrite()){
            log.error("Don't have permission to write into the given folder: " + filePath);
            throw new AccessDeniedException("Don't have permission to write into the given folder: " + filePath);
        }

        return new OneFile(file.isDirectory(), file.getName());
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

    public Path uploadFile(MultipartFile file, boolean isFolder, String destinationFolder) throws IOException {
        Path uploadPath = Paths.get(destinationFolder, file.getOriginalFilename());
        File checkabelFile = new File(destinationFolder);

        File[] filesInDir = checkabelFile.listFiles();
        String fileName = file.getOriginalFilename();
        if(isFolder){
            fileName = file.getOriginalFilename().replaceFirst(".zip", "");
        }
        String finalFileName = fileName;
        if(Arrays.stream(filesInDir).anyMatch(fileInDir -> fileInDir.getName().equals(finalFileName))){
            return null;
        }

        if(isFolder){
            String originalFolderName = file.getOriginalFilename().replaceFirst(".zip", "");
            uploadPath = Paths.get(destinationFolder, originalFolderName);
            ZipService.unzip(file, uploadPath.toString());
        }
        else{
           file.transferTo(uploadPath);
        }

        return uploadPath;
    }

    public Map<String, String> createFolder(String destinationFolder, String newFolderName){
        File newFolder = new File(destinationFolder + "/" + newFolderName);
        boolean resultOfNewFolderCreation = newFolder.mkdir();
        if(resultOfNewFolderCreation){
            return Map.of("Success", newFolder.toString());
        }
        return Map.of("Failure", "The folder could not be created.");
    }
}
