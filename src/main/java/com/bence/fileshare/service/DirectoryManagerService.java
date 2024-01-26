package com.bence.fileshare.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DirectoryManagerService {
    @Value("${my_root_directory}")
    private String rootDirectory;
    private String trashDirectory = "";
    private String dataDirectory = "";

    public void setRootDirectory(String value){
        if(!rootDirectory.equals("none")){
            return;
        }
        rootDirectory = value;
    }

    public void setTrashDirectory(String trashDirectory) {
        if(!trashDirectory.isEmpty()){
            return;
        }
        this.trashDirectory = trashDirectory;
    }

    public void setDataDirectory(String dataDirectory) {
        if(!dataDirectory.isEmpty()){
            return;
        }
        this.dataDirectory = dataDirectory;
    }

    public String getRootDirectory() {
        if(rootDirectory.equals("none")){
            return "";
        }
        return rootDirectory;
    }

    public String getTrashDirectory() {
        if(trashDirectory == null){
            return "";
        }
        return trashDirectory;
    }

    public String getDataDirectory() {
        if(dataDirectory == null){
            return "";
        }
        return dataDirectory;
    }
}
