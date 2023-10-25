package com.bence.fileshare.utils;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipClass {

    public static void addFolderToZip(String parentPath, Path sourceFolder, ZipOutputStream zipOutputStream) throws IOException {
        Files.list(sourceFolder).forEach(path -> {
            try {
                String entryName = parentPath + path.getFileName().toString();
                if (Files.isDirectory(path)) {
                    zipOutputStream.putNextEntry(new ZipEntry(entryName + "/"));
                    addFolderToZip(entryName + "/", path, zipOutputStream);
                } else {
                    zipOutputStream.putNextEntry(new ZipEntry(entryName));
                    Files.copy(path, zipOutputStream);
                    zipOutputStream.closeEntry();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
