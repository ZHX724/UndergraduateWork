package org.demo.blockchaintracksystem.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.demo.blockchaintracksystem.entity.Batch;
import org.demo.blockchaintracksystem.service.BatchService;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/batch")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @PostMapping("/create")
    public String create(@RequestParam Long productId,
                         @RequestParam String batchCode,
                         @RequestParam(required = false) String productionDate,
                         HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) return "未登录";
        return batchService.create(productId, batchCode, productionDate);
    }

    @GetMapping("/list")
    public List<Batch> list(@RequestParam Long productId, HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) return Collections.emptyList();
        return batchService.list(productId);
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long batchId, HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) return "未登录";
        return batchService.delete(batchId);
    }
}
