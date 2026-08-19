package com.company.virs.repository;

import com.company.virs.entity.BatchExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BatchExecutionRepository extends JpaRepository<BatchExecution, UUID> {

    Optional<BatchExecution> findByFileName(String fileName);

    List<BatchExecution> findByStatus(String status);

    boolean existsByFileName(String fileName);
}