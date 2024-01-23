package com.bence.fileshare.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class FileDeleteService {
    private static String trashDirectoryPath;
    @Value("${my_root_directory}")
    private String rootDirectoryPath;

    private File getTrashDirectory() throws Exception {
        if(trashDirectoryPath != null && !trashDirectoryPath.isEmpty()){
            return new File(trashDirectoryPath);
        }

        File rootDirectoryFile = new File(rootDirectoryPath);
        File parentOfRoot = rootDirectoryFile.getParentFile();
        File trashDirectoryFile = new File(parentOfRoot.getPath() + "/" + "fileshare_trash");
        if(trashDirectoryFile.mkdir()){
            trashDirectoryPath = trashDirectoryFile.getPath();
            return new File(trashDirectoryPath);
        }

        throw new Exception("Could not create trash directory at: " + trashDirectoryFile.getPath());
    }

    private boolean copyWithSizeCheck(File toBeCopiedFile, File destinationFile) throws IOException {
        boolean copySuccess;
        int numberOfTries = 0;
        int numberOfMaxTries = 3;

        do {
            copySuccess = true;
            int bytesCopied = FileCopyUtils.copy(toBeCopiedFile, destinationFile);
            if (bytesCopied != FileUtils.sizeOf(toBeCopiedFile)) {
                log.warn("The copied files size does not match with the original! File: " + toBeCopiedFile.getPath());
                destinationFile.delete();
                copySuccess = false;
                numberOfTries++;
            }
        }
        while(!copySuccess && numberOfTries < numberOfMaxTries);

        if(numberOfTries == numberOfMaxTries){
            log.error("The copy of file: " + toBeCopiedFile.getPath() + " was unsuccessfull!");
            return false;
        }

        return true;
    }

    public boolean deleteFile(String filePath) throws Exception {
        File trashDirectory = getTrashDirectory();

        File toBeDeletedFile = new File(filePath);
        if(!(toBeDeletedFile.canWrite() && toBeDeletedFile.canRead())){
            log.error("You don't have permission to edit this file: " + filePath);
            return false;
        }

        File destinationFile = new File(trashDirectory.getPath() + "/" + toBeDeletedFile.getName());

        boolean resultOfCopy = copyWithSizeCheck(toBeDeletedFile, destinationFile);

        if(!resultOfCopy){
            log.error("The following file could not be placed inside the trash: " + toBeDeletedFile.getPath());
            throw new Exception("The following file could not be placed inside the trash: " + toBeDeletedFile.getPath());
        }

        //TODO There is going to be a database row insert here. About which file was successfully copied, so that it later can be maintained.

        return true;
    }
}
