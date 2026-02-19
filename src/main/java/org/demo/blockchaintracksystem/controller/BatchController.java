package org.demo.blockchaintracksystem.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.demo.blockchaintracksystem.entity.Batch;
import org.demo.blockchaintracksystem.mapper.BatchMapper;
import org.demo.blockchaintracksystem.service.BatchService;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/batch")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;
    private final BatchMapper batchMapper;

    @GetMapping("/byCode")
    public Batch byCode(@RequestParam String batchCode) {
        return batchMapper.selectByCode(batchCode);
    }

    @GetMapping("/all")
    public List<Batch> all() {
        return batchMapper.selectAll();
    }

    // 监管端：标记/取消风险
    @PostMapping("/risk")
    public String risk(@RequestParam Long id,
                       @RequestParam Integer riskFlag,
                       @RequestParam(required = false) String riskNote) {
        batchMapper.updateRisk(id, riskFlag, riskNote);
        return "OK";
    }
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
