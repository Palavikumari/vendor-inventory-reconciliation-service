package com.company.virs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "batch_execution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchExecution {

    @Id
    @Column(name = "batch_id")
    private UUID batchId;

    @Column(
            name = "file_name",
            nullable = false,
            length = 255)
    private String fileName;

    @Column(
            name = "execution_type",
            nullable = false,
            length = 50)
    private String executionType;

    @Column(
            name = "status",
            nullable = false,
            length = 30)
    private String status;

    @Column(
            name = "total_records")
    private Integer totalRecords;

    @Column(
            name = "processed_records")
    private Integer processedRecords;

    @Column(
            name = "failed_records")
    private Integer failedRecords;

    @Column(
            name = "batch_size",
            nullable = false)
    private Integer batchSize;

    @Column(
            name = "start_time",
            nullable = false)
    private LocalDateTime startTime;

    @Column(
            name = "end_time")
    private LocalDateTime endTime;

    @OneToMany(
            mappedBy = "batchExecution",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<VendorInventory> vendorInventories;
}