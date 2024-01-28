package com.bence.fileshare.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "fileshare_users")
@Data
public class UsersEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "name", nullable = false)
    String name;

    public UsersEntity() {
    }

    public UsersEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
