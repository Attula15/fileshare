package com.bence.fileshare.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DirectoryManagerService {

    @Getter
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
        if(!this.trashDirectory.isEmpty()){
            return;
        }
        this.trashDirectory = trashDirectory;
    }

    public void setDataDirectory(String dataDirectory) {
        if(!this.dataDirectory.isEmpty()){
            return;
        }
        this.dataDirectory = dataDirectory;
    }

    public String getTrashDirectory() {
        if(trashDirectory.isEmpty()){
            trashDirectory = rootDirectory + "/fileshare_trash";
        }
        return trashDirectory;
    }

    public String getDataDirectory() {
        if(dataDirectory.isEmpty()){
            dataDirectory = rootDirectory + "/customer_data";
        }
        return dataDirectory;
    }
}
