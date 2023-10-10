package com.bence.fileshare.service;

import com.bence.fileshare.pojo.FolderInfo;
import com.bence.fileshare.utils.FileSizeConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FileAccessService {
    public FolderInfo getInfo(String folderPath) throws AccessDeniedException {
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

        List<Map<String, File>> list = new ArrayList<>();

        if(dirListing != null){
            for(File file : dirListing){
                list.add(Map.of(file.isDirectory() ? "Dir" : "File", file));
            }
            returnable.setFilesInDir(list);
        }

        returnable.setFolderSize(FileSizeConverter.convert(FileUtils.sizeOfDirectory(folder)));

        return returnable;
    }
}
