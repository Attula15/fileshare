package com.bence.fileshare.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipService {
    public static void unzip(MultipartFile zipFile, String destDirectory) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipFile.getBytes()))) {
            byte[] buffer = new byte[1024];

            // create destination directory if not exists
            File destDir = new File(destDirectory);
            if (!destDir.exists()) {
                destDir.mkdir();
            }

            // iterate over entries in the zip file
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null) {
                String filePath = destDirectory + File.separator + zipEntry.getName();
                if (!zipEntry.isDirectory()) {
                    // if the entry is a file, extract it
                    extractFile(zipInputStream, filePath);
                } else {
                    // if the entry is a directory, create it
                    File dir = new File(filePath);
                    dir.mkdir();
                }
                zipEntry = zipInputStream.getNextEntry();
            }
        }
    }

    private static void extractFile(ZipInputStream zipInputStream, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
        }
    }
}
