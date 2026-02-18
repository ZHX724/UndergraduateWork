package org.demo.blockchaintracksystem.controller;

import jakarta.servlet.http.HttpSession;
import org.demo.blockchaintracksystem.entity.Batch;
import org.demo.blockchaintracksystem.mapper.BatchMapper;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/batch")
public class BatchController {

    private final BatchMapper batchMapper;

    public BatchController(BatchMapper batchMapper) {
        this.batchMapper = batchMapper;
    }

    // 创建批次（必须是自己名下的 product）
    @PostMapping("/create")
    public String create(@RequestParam Long productId,
                         @RequestParam String batchCode,
                         @RequestParam(required = false) String productionDate, // "2026-02-11"
                         HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) return "未登录";

        if (productId == null) return "productId 不能为空";
        if (batchCode == null || batchCode.trim().isEmpty()) return "批次号不能为空";

        // 权限：product 必须属于当前用户
        if (batchMapper.countProductOwnedByUid(productId, uid) <= 0) {
            return "无权限（该农产品不属于你）";
        }

        Batch b = new Batch();
        b.setProductId(productId);
        b.setBatchCode(batchCode.trim());

        // production_date 可选：不传就 null
        if (productionDate != null && !productionDate.trim().isEmpty()) {
            b.setProductionDate(java.time.LocalDate.parse(productionDate.trim()));
        }

        batchMapper.insert(b);
        return "创建成功";
    }

    // 查询某个产品的批次列表（也要校验 product 属于你）
    @GetMapping("/list")
    public List<Batch> list(@RequestParam Long productId, HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) return Collections.emptyList();
        if (productId == null) return Collections.emptyList();

        if (batchMapper.countProductOwnedByUid(productId, uid) <= 0) {
            return Collections.emptyList();
        }

        return batchMapper.selectByProductId(productId);
    }

    // 删除批次（只能删自己的）
    @PostMapping("/delete")
    public String delete(@RequestParam Long batchId, HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) return "未登录";
        if (batchId == null) return "batchId 不能为空";

        int n = batchMapper.deleteByIdAndUid(batchId, uid);
        return n > 0 ? "删除成功" : "删除失败（无权限或不存在）";
    }
}
