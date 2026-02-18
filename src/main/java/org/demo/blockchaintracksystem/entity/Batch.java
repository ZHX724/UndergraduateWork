package org.demo.blockchaintracksystem.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Batch {
    private Long id;
    private Long productId;
    private String batchCode;
    private LocalDate productionDate;
    private LocalDateTime createdAt;
}
