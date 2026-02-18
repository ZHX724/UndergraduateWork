package org.demo.blockchaintracksystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}
