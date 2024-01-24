package com.bence.fileshare.service;

import com.bence.fileshare.pojo.FolderInfo;
import com.bence.fileshare.pojo.OneFile;
import com.bence.fileshare.utils.FileSizeConverter;
import jdk.jshell.spi.ExecutionControl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
@Slf4j
public class FileAccessService {
    private final DirectoryManagerService directoryManagerService;
    private String rootTxt = ".root.txt";

    public FileAccessService(DirectoryManagerService directoryManagerService) {
        this.directoryManagerService = directoryManagerService;
    }

    ///TODO
    private File getRootDirectory() throws Exception {
        if(directoryManagerService.getRootDirectory().isEmpty()){
            log.info("Root directory is empty, initializing root directory to default");
            String os = System.getProperty("os.name");

            if(os.contains("Windows")){
                log.error("The operating system is Windows, could not create default root directory");
                throw new ExecutionControl.NotImplementedException("The application currently does not fully support " +
                        "Windows based operating systems. " +
                        "Please make sure to set the root directory to an existing directory!");
            }

            File defaultRootDirectory = new File("/opt/fileshare_rootDirectory");
            File rootDirectoryTextFile = new File("/opt/fileshare_rootDirectory/" + rootTxt);///TODO I don't think that this is going to work

            if(defaultRootDirectory.exists()){
                log.info("The default root directory (/opt/fileshare_rootDirectory) already exists!");
                log.info("Checking if the default directory was made by the program before");
                if(rootDirectoryTextFile.exists()){
                    File rootDirectoryData = new File("/opt/fileshare_rootDirectory/customer_data");
                    if(!rootDirectoryData.exists()){
                        boolean dataDirResult = rootDirectoryData.mkdir();
                        if(!dataDirResult){
                            log.error("Could not create data directory (/opt/fileshare_rootDirectory/customer_data)!");
                            throw new Exception("Could not create data directory (/opt/fileshare_rootDirectory/customer_data)!");
                        }
                    }
                    else{
                        log.warn("The data directory already exists");
                    }
                    directoryManagerService.setDataDirectory(rootDirectoryData.getPath());
                    return new File(directoryManagerService.getRootDirectory());
                }
                log.error("The default root directory (/opt/fileshare_rootDirectory) already exists and was not made by this program!");
                throw new Exception("The default root directory (/opt/fileshare_rootDirectory) already exists and was not made by this program!");
            }

            if(!defaultRootDirectory.mkdir()){
                log.error("Could not create default folder!");
                throw new Exception("Could not create default folder: /opt/fileshare_rootDirectory");
            }

            log.info("Setting permissions to the root folder");
            if(!(defaultRootDirectory.setReadable(true)
                    && defaultRootDirectory.setWritable(true)
                    && defaultRootDirectory.setExecutable(true))){
                log.error("There was an error while setting the privileges. (/opt/fileshare_rootDirectory)");
                throw new Exception("There was an error while setting the privileges. (/opt/fileshare_rootDirectory)");
            }
            log.info("Permissions has been set");

            if(!rootDirectoryTextFile.createNewFile()){
                log.error("Could not create checker txt file!");
                throw new Exception("Could not create checker txt file!");
            }

            log.info("Checker txt file has been created");
            File rootDirectoryData = new File("/opt/fileshare_rootDirectory/customer_data");
            rootDirectoryData.mkdir();

            directoryManagerService.setDataDirectory(rootDirectoryData.getPath());
        }
        else {
            File rootDir = new File(directoryManagerService.getRootDirectory());
            if(!rootDir.exists()){
                log.error("The given root directory does not exists!");
                throw new Exception("The given root directory does not exists!");
            }
            else if (!(rootDir.canRead() && rootDir.canWrite())) {
                log.error("Don't have enough permissions to the root directory!");
                throw new Exception("Don't have enough permissions to the root directory!");
            }
            else{
                File rootDirectoryData = new File(directoryManagerService.getRootDirectory() + "/customer_data");
                rootDirectoryData.mkdir();
                directoryManagerService.setDataDirectory(rootDirectoryData.getPath());
            }
        }
        createTrashFolder();

        log.info("Main directory structure has been created successfully.");

        return new File(directoryManagerService.getRootDirectory());
    }

    private void createTrashFolder(){
        ///TODO
    }

    private String addGivenPathToRootFoldersPath(String filePath){
        if(filePath.isEmpty()){
            filePath = directoryManagerService.getDataDirectory();
        }
        else{
            filePath = directoryManagerService.getDataDirectory() + "/" + filePath;
        }

        return filePath;
    }

    public FolderInfo getInfo(String folderPath) throws Exception {
        getRootDirectory();

        folderPath = addGivenPathToRootFoldersPath(folderPath);

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
            if(!folder.getPath().equals(directoryManagerService.getDataDirectory())){
                for(File file : dirListing){
                    list.add(new OneFile(file.isDirectory(), file.getName()));
                }
            }
            else{
                for(File file : dirListing){
                    if(!file.getName().equals(rootTxt)){
                        list.add(new OneFile(file.isDirectory(), file.getName()));
                    }
                }
            }
            returnable.setFilesInDir(list);
        }

        returnable.setFolderSize(FileSizeConverter.convert(FileUtils.sizeOfDirectory(folder)));

        return returnable;
    }

    public OneFile getOneFileInfo(String filePath) throws Exception {
        getRootDirectory();

        filePath = addGivenPathToRootFoldersPath(filePath);
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
        uploadPath = Paths.get(addGivenPathToRootFoldersPath(uploadPath.toString()));
        File checkabelFile = new File(destinationFolder);

        File[] filesInDir = checkabelFile.listFiles();
        String fileName = file.getOriginalFilename();
        if(isFolder){
            fileName = file.getOriginalFilename().replaceFirst(".zip", "");
        }
        String finalFileName = fileName;
        if(filesInDir != null && Arrays.stream(filesInDir).anyMatch(fileInDir -> fileInDir.getName().equals(finalFileName))){
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
        destinationFolder = addGivenPathToRootFoldersPath(destinationFolder);
        File newFolder = new File(destinationFolder + "/" + newFolderName);
        boolean resultOfNewFolderCreation = newFolder.mkdir();
        if(resultOfNewFolderCreation){
            return Map.of("Success", newFolder.toString());
        }
        return Map.of("Failure", "The folder could not be created.");
    }

    private void deleteFilesRecursively(String folderPath){
        File toBeDeletedFile = new File(folderPath);
        if(!toBeDeletedFile.canWrite()){
            log.error("Insufficient permission for the give file: " + toBeDeletedFile.getPath());
            throw new SecurityException("Insufficient permission for the give file: " + toBeDeletedFile.getPath());
        }
        if(toBeDeletedFile.isDirectory()){
            File[] files = toBeDeletedFile.listFiles();
            for (File file : files) {
                deleteFilesRecursively(file.getPath());
            }
        }
        log.warn("Deleting " + toBeDeletedFile.getPath());
        toBeDeletedFile.delete();
    }

    public Map<String, String> delete(String filePath) throws SecurityException{
        filePath = addGivenPathToRootFoldersPath(filePath);

        if(filePath.equals(directoryManagerService.getDataDirectory())){
            return Map.of("Failure", "The filepath is the data path!");
        }
        else if(filePath.equals(directoryManagerService.getRootDirectory())){
            return Map.of("Failure", "The filepath is the root path!");
        }
        else if(filePath.equals(directoryManagerService.getTrashDirectory())){
            return Map.of("Failure", "The filepath is the trash path!");
        }

        File toBeDeletedFile = new File(filePath);

        if(toBeDeletedFile.isDirectory() && toBeDeletedFile.listFiles().length > 0){
            deleteFilesRecursively(toBeDeletedFile.getPath());
        }

        boolean success = toBeDeletedFile.delete();
        if(success){
            return Map.of("Success", "The file: "+ filePath +" has been deleted.");
        }
        return Map.of("Failure", "The file: " + filePath + " could not be deleted");
    }
}
