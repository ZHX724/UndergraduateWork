package org.demo.blockchaintracksystem.controller;

import jakarta.servlet.http.HttpSession;
import org.demo.blockchaintracksystem.service.BlockchainService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trace")
public class TraceController {

    private final BlockchainService blockchainService;

    public TraceController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    // 上链：新增一条溯源记录（自动生成 hash 链字段）
    @PostMapping("/add")
    public String add(@RequestParam Long batchId,
                      @RequestParam String stage,
                      @RequestParam String content,
                      HttpSession session) {

        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) return "未登录";

        TraceRecord r = new TraceRecord();
        r.setBatchId(batchId);
        r.setStage(stage);
        r.setContent(content);
        r.setOperator(String.valueOf(session.getAttribute("username"))); // 或 uid
        blockchainService.append(r);

        return "上链成功";
    }

    // 校验链是否被篡改
    @GetMapping("/verify")
    public Object verify(@RequestParam Long batchId) {
        return blockchainService.verify(batchId);
    }

    // 查询链（给前端时间线展示）
    @GetMapping("/chain")
    public List<TraceRecord> chain(@RequestParam Long batchId) {
        // 你也可以直接 mapper 查出来返回
        // 这里省略：为了简洁
        return List.of();
    }
}

