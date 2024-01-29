package com.bence.fileshare.repository;

import com.bence.fileshare.entity.DeletedFilesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

@Repository
public interface DeletedFilesRepository extends JpaRepository<DeletedFilesEntity, Long> {
}
