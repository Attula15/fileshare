package com.bence.fileshare.pojo;

import lombok.Data;

@Data
public class OneFile {
    boolean isFolder;
    String name;

    public OneFile(boolean isFolder, String name) {
        this.isFolder = isFolder;
        this.name = name;
    }
}
