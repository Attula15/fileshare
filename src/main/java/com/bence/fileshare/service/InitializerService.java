package com.bence.fileshare.service;

import com.bence.fileshare.repository.DeletedFilesRepository;
import com.bence.fileshare.repository.UsersRepository;
import jdk.jshell.spi.ExecutionControl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
@Slf4j
@Order(1)
public class InitializerService {
    @Value("${init}")
    private String initValue;
    private boolean shouldInit;
    private DirectoryManagerService directoryManagerService;
    private UsersRepository usersRepository;
    private DeletedFilesRepository deletedFilesRepository;


    public InitializerService(DirectoryManagerService directoryManagerService, UsersRepository usersRepository, DeletedFilesRepository deletedFilesRepository) {
        this.directoryManagerService = directoryManagerService;
        this.usersRepository = usersRepository;
        this.deletedFilesRepository = deletedFilesRepository;
    }

    ///TODO
    public File getRootDirectory() throws Exception {
        shouldInit = (initValue.equals("true") || initValue.equals("True") || initValue.equals("TRUE"));
        if(shouldInit){
            if(directoryManagerService.getRootDirectory().isEmpty()){
                log.info("There was no directory given");
                initalizeDefaultDirectory();
            }
            else {
                log.info("There was a directory given");
                initializeGivenDirectory();
            }
            createDatabase();
            log.info("Main directory structure has been created successfully.");
        }
        //When it already has been created before
        else{
            if(directoryManagerService.getRootDirectory().isEmpty()){
                directoryManagerService.setRootDirectory("/opt/fileshare_rootDirectory");
            }
            directoryManagerService.setDataDirectory(directoryManagerService.getRootDirectory() + "/customer_data");
            directoryManagerService.setTrashDirectory(directoryManagerService.getRootDirectory() + "/fileshare_trash");
            return new File(directoryManagerService.getRootDirectory());
        }

        return new File(directoryManagerService.getRootDirectory());
    }

    private void initalizeDefaultDirectory() throws Exception{
        log.info("Initializing root directory to default");
        String os = System.getProperty("os.name");

        if(os.contains("Windows")){
            log.error("The operating system is Windows, could not create default root directory");
            throw new ExecutionControl.NotImplementedException("The application currently does not fully support " +
                    "Windows based operating systems. " +
                    "Please make sure to set the root directory to an existing directory!");
        }

        createDirectoryStructureOnLinux();
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

    private void createDirectoryStructureOnLinux() throws Exception {
        File defaultRootDirectory = new File("/opt/fileshare_rootDirectory");

        //Delete the original directory
        if(defaultRootDirectory.exists()){
            deleteFilesRecursively(defaultRootDirectory.getPath());
        }

        if(!defaultRootDirectory.mkdir()){
            log.error("Could not create default folder!");
            throw new Exception("Could not create default folder: /opt/fileshare_rootDirectory");
        }
        log.info("Successfully created root directory");

        log.info("Setting permissions to the root folder");
        if(!(defaultRootDirectory.setReadable(true)
                && defaultRootDirectory.setWritable(true)
                && defaultRootDirectory.setExecutable(true))){
            log.error("There was an error while setting the privileges. (/opt/fileshare_rootDirectory)");
            throw new Exception("There was an error while setting the privileges. (/opt/fileshare_rootDirectory)");
        }
        log.info("Permissions has been set");
        directoryManagerService.setRootDirectory(defaultRootDirectory.getPath());

        createDataFolder();
        createTrashFolder();
    }

    private void initializeGivenDirectory() throws Exception{
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
            createDataFolder();
            createTrashFolder();
        }
    }

    private void createDataFolder(){
        File rootDataDirectory = new File(directoryManagerService.getRootDirectory() + "/customer_data");

        log.info("Creating data directory at: " + rootDataDirectory.getPath());
        rootDataDirectory.mkdir();

        log.info("Data directory has been created");
        directoryManagerService.setDataDirectory(rootDataDirectory.getPath());
    }

    private void createTrashFolder(){
        File trashDir = new File(directoryManagerService.getRootDirectory() + "/fileshare_trash");

        log.info("Creating trash directory at: " + trashDir.getPath());
        trashDir.mkdir();

        log.info("Trash directory has been created");
        directoryManagerService.setTrashDirectory(trashDir.getPath());
    }

    private void createDatabase(){
        usersRepository.findAll();
        deletedFilesRepository.findAll();
    }
}
