package com.bence.fileshare.service;

import com.bence.fileshare.repository.DeletedFilesRepository;
import com.bence.fileshare.repository.UsersRepository;
import jdk.jshell.spi.ExecutionControl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

@Service
@Slf4j
public class InitializerService {
    private DirectoryManagerService directoryManagerService;
    private String rootTxt = ".root.txt";

    private UsersRepository usersRepository;
    private DeletedFilesRepository deletedFilesRepository;


    public InitializerService(DirectoryManagerService directoryManagerService, UsersRepository usersRepository, DeletedFilesRepository deletedFilesRepository) {
        this.directoryManagerService = directoryManagerService;
        this.usersRepository = usersRepository;
        this.deletedFilesRepository = deletedFilesRepository;
    }

    ///TODO
    public File getRootDirectory() throws Exception {
        if(directoryManagerService.getRootDirectory().isEmpty()){
            log.info("There was no directory given");
            initalizeDefaultDirectory();
        }
        else {
            log.info("There was a directory given");
            initializeGivenDirectory();
        }

        createTrashFolder();
        createDatabase();

        log.info("Main directory structure has been created successfully.");

        File rootDirectoryTextFile = new File("/opt/fileshare_rootDirectory/" + rootTxt);
        FileWriter fileWriter = new FileWriter(rootDirectoryTextFile);
        fileWriter.write("Everything is alright!");
        fileWriter.close();

        log.info("Checker file filled with information successfully");

        return new File(directoryManagerService.getRootDirectory());
    }

    private void initalizeDefaultDirectory() throws Exception{
        File rootDirectoryTextFile = new File("/opt/fileshare_rootDirectory/" + rootTxt);

        //Make sure that the initialize process stops in case it has already been run.
        if(directoryManagerService.getDataDirectory().isEmpty() ||
                directoryManagerService.getTrashDirectory().isEmpty() ||
                directoryManagerService.getRootDirectory().isEmpty()) {
            if (rootDirectoryTextFile.exists()) {
                Scanner scanner = new Scanner(rootDirectoryTextFile);
                while (scanner.hasNextLine()) {
                    String row = scanner.nextLine();
                    if (row.contains("Everything is alright!")) {
                        scanner.close();
                        log.error("The directory structure is corrupted!");
                        throw new Exception("The directory structure seems to be corrupted, a complete reinstall is needed. " +
                                "See the documentation for further information");
                    }
                }
                scanner.close();
            }
        }

        log.info("Root directory is empty, initializing root directory to default");
        String os = System.getProperty("os.name");

        if(os.contains("Windows")){
            log.error("The operating system is Windows, could not create default root directory");
            throw new ExecutionControl.NotImplementedException("The application currently does not fully support " +
                    "Windows based operating systems. " +
                    "Please make sure to set the root directory to an existing directory!");
        }

        createDirectoryStructureOnLinux(rootDirectoryTextFile);
    }

    private void createDirectoryStructureOnLinux(File rootDirectoryTextFile) throws Exception {
        File defaultRootDirectory = new File("/opt/fileshare_rootDirectory");

        if(defaultRootDirectory.exists()){
            log.info("The default root directory (/opt/fileshare_rootDirectory) already exists!");
            log.info("Checking if the default directory was made by this program before");
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
                directoryManagerService.setRootDirectory(defaultRootDirectory.getPath());
                return;
            }
            log.error("The default root directory (/opt/fileshare_rootDirectory) already exists and was not made by this program!");
            throw new Exception("The default root directory (/opt/fileshare_rootDirectory) already exists and was not made by this program!");
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

        if(!rootDirectoryTextFile.createNewFile()){
            log.error("Could not create checker txt file!");
            throw new Exception("Could not create checker txt file!");
        }

        log.info("Checker txt file has been created");
        File rootDirectoryData = new File("/opt/fileshare_rootDirectory/customer_data");
        rootDirectoryData.mkdir();

        directoryManagerService.setDataDirectory(rootDirectoryData.getPath());
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
            File rootDirectoryData = new File(directoryManagerService.getRootDirectory() + "/customer_data");
            rootDirectoryData.mkdir();
            directoryManagerService.setDataDirectory(rootDirectoryData.getPath());
        }
    }

    private void createTrashFolder() throws Exception{
        if(!directoryManagerService.getTrashDirectory().isEmpty()){
            return;
        }

        File trashDir = new File(directoryManagerService.getRootDirectory() + "/fileshare_trash");
        log.info("Creating trash directory at: " + directoryManagerService.getRootDirectory() + "/fileshare_trash");
        if(!trashDir.mkdir()){
            log.error("The trash directory could not be created!");
            throw new Exception("The trash directory could not be created!");
        }

        log.info("Trash folder has been created");
        directoryManagerService.setTrashDirectory(trashDir.getPath());
    }

    private void createDatabase() throws Exception{
        usersRepository.findAll();
        deletedFilesRepository.findAll();
    }
}
