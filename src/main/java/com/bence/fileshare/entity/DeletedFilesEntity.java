package com.bence.fileshare.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.context.annotation.DependsOn;

import java.time.LocalDateTime;

@Entity
@Table(name="deleted_files")
@Data
public class DeletedFilesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "delete_date")
    LocalDateTime deleteDate;

    @Column(name = "user_id")
    Long userId;

    @Column(name = "file_name")
    String fileName;

    @Column(name = "original_file_path")
    String originalFilePath;

    public DeletedFilesEntity() {
    }

    public DeletedFilesEntity(Long id, LocalDateTime deleteDate, Long userId, String fileName, String originalFilePath) {
        this.id = id;
        this.deleteDate = deleteDate;
        this.userId = userId;
        this.fileName = fileName;
        this.originalFilePath = originalFilePath;
    }
}
