package org.demo.blockchaintracksystem.service;

import lombok.RequiredArgsConstructor;
import org.demo.blockchaintracksystem.entity.Batch;
import org.demo.blockchaintracksystem.mapper.BatchMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchService {

    private final BatchMapper batchMapper;

    public String create(Long productId, String batchCode, String productionDate) {
        if (productId == null) return "productId 不能为空";
        if (batchCode == null || batchCode.trim().isEmpty()) return "batchCode 不能为空";

        Batch b = new Batch();
        b.setProductId(productId);
        b.setBatchCode(batchCode.trim());
        b.setCreatedAt(LocalDateTime.now().withNano(0));
        b.setRiskFlag(0);
        b.setRiskNote(null);


        if (productionDate != null && !productionDate.trim().isEmpty()) {
            // 前端传 yyyy-MM-dd
            b.setProductionDate(LocalDate.parse(productionDate.trim()));
        }

        batchMapper.insert(b);
        return "创建成功 id=" + b.getId();
    }

    public List<Batch> list(Long productId) {
        return batchMapper.selectByProductId(productId);
    }

    public String delete(Long id) {
        if (id == null) return "batchId 不能为空";
        int n = batchMapper.deleteById(id);
        return n > 0 ? "删除成功" : "未找到该批次";
    }
}
