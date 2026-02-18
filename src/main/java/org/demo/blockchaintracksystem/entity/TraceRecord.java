package org.demo.blockchaintracksystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TraceRecord {
    private Long id;

    private Long batchId;
    private String type;
    private String content;
    private String status;

    private LocalDateTime createdAt;

    private String prevHash;
    private String dataHash;
    private String blockHash;

    private Integer nonce;
    private Integer height;
}
