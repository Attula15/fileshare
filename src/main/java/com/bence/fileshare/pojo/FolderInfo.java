package com.bence.fileshare.pojo;

import lombok.Data;

import java.io.File;
import java.util.List;
import java.util.Map;

@Data
public class FolderInfo {
    private List<Map<String, File>> filesInDir;
    private SizeClass folderSize;
}
