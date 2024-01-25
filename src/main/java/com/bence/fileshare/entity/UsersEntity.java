package com.bence.fileshare.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class UsersEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;

    public UsersEntity() {
    }

    public UsersEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
