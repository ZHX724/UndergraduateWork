package org.demo.blockchaintracksystem.controller;

import jakarta.servlet.http.HttpSession;
import org.demo.blockchaintracksystem.entity.Batch;
import org.demo.blockchaintracksystem.entity.TraceRecord;
import org.demo.blockchaintracksystem.mapper.BatchMapper;
import org.demo.blockchaintracksystem.mapper.TraceRecordMapper;
import org.demo.blockchaintracksystem.service.BlockchainService;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/trace")
public class TraceController {

    private final BlockchainService blockchainService;
    private final TraceRecordMapper traceRecordMapper;
    private final BatchMapper batchMapper;

    public TraceController(BlockchainService blockchainService, TraceRecordMapper traceRecordMapper, BatchMapper batchMapper) {
        this.blockchainService = blockchainService;
        this.traceRecordMapper = traceRecordMapper;
        this.batchMapper = batchMapper;
    }

    // 新增一条溯源记录
    @PostMapping("/add")
    public String add(@RequestParam Long batchId,
                      @RequestParam String type,
                      @RequestParam String content,
                      HttpSession session) {

        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) return "未登录";

        String safeType = (type == null) ? "" : type.trim().toUpperCase();
        if (safeType.isEmpty()) return "type 不能为空";
        if (content == null || content.trim().isEmpty()) return "content 不能为空";

        return blockchainService.append(batchId, safeType, content.trim());
    }

    // 校验链是否被篡改
    @GetMapping("/verify")
    public Object verify(@RequestParam Long batchId) {
        return blockchainService.verify(batchId);
    }

    // 查询链，给前端时间线展示
    @GetMapping("/chain")
    public List<TraceRecord> chain(@RequestParam Long batchId) {
        return traceRecordMapper.selectByBatchIdOrderByHeight(batchId);
    }
    @GetMapping("/chainByCode")
    public List<TraceRecord> chainByCode(@RequestParam String batchCode) {
        Batch b = batchMapper.selectByBatchCode(batchCode);
        if (b == null) return List.of();
        return traceRecordMapper.selectByBatchIdOrderByHeight(b.getId());
    }


}


