package com.bence.fileshare.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name="deleted_files")
@Data
public class DeletedFilesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    LocalDateTime deleteDate;
    @OneToOne(targetEntity = UsersEntity.class)
    Long deletedById;
    String fileName;
    String originalFilePath;

    public DeletedFilesEntity() {
    }

    public DeletedFilesEntity(Long id, LocalDateTime deleteDate, Long deletedById, String fileName, String originalFilePath) {
        this.id = id;
        this.deleteDate = deleteDate;
        this.deletedById = deletedById;
        this.fileName = fileName;
        this.originalFilePath = originalFilePath;
    }
}
