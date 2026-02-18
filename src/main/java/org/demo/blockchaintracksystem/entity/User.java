package org.demo.blockchaintracksystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private Integer role;   // 0=USER
    private Integer status; // 1=enabled
    private LocalDateTime createdAt;
}
