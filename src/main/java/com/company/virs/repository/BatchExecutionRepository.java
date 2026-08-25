package com.company.virs.repository;

import com.company.virs.entity.BatchExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BatchExecutionRepository
        extends JpaRepository<BatchExecution, UUID> {
}