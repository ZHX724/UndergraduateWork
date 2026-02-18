package org.demo.blockchaintracksystem.service;

import lombok.RequiredArgsConstructor;
import org.demo.blockchaintracksystem.entity.TraceRecord;
import org.demo.blockchaintracksystem.mapper.TraceRecordMapper;
import org.demo.blockchaintracksystem.utils.HashUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BlockchainService {

    private final TraceRecordMapper traceRecordMapper;

    // 难度：前导 0 的数量（太大你会卡死）
    private static final int DIFFICULTY = 4;

    public String append(Long batchId, String type, String content) {
        TraceRecord last = traceRecordMapper.selectLastByBatchId(batchId);

        int nextHeight = (last == null || last.getHeight() == null) ? 1 : last.getHeight() + 1;
        String prevHash = (last == null) ? "0" : last.getBlockHash();

        LocalDateTime now = LocalDateTime.now();

        // dataHash：把“业务数据”固定成摘要（不含 nonce）
        String dataPlain = batchId + "|" + nextHeight + "|" + type + "|" + content + "|" + now + "|" + prevHash;
        String dataHash = HashUtil.sha256(dataPlain);

        // blockHash：prevHash + dataHash + nonce + height 做 PoW
        int nonce = 0;
        String blockHash;
        String prefix = "0".repeat(DIFFICULTY);

        while (true) {
            String blockPlain = prevHash + "|" + dataHash + "|" + nonce + "|" + nextHeight;
            blockHash = HashUtil.sha256(blockPlain);
            if (blockHash.startsWith(prefix)) break;
            nonce++;
        }

        TraceRecord r = new TraceRecord();
        r.setBatchId(batchId);
        r.setType(type);
        r.setContent(content);
        r.setStatus("OK");
        r.setCreatedAt(now);
        r.setPrevHash(prevHash);
        r.setDataHash(dataHash);
        r.setBlockHash(blockHash);
        r.setNonce(nonce);
        r.setHeight(nextHeight);

        traceRecordMapper.insert(r);

        return "上链成功 height=" + nextHeight + " hash=" + blockHash;
    }

    public Map<String, Object> verify(Long batchId) {

        List<TraceRecord> chain = traceRecordMapper.selectByBatchIdOrderByHeight(batchId);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("batchId", batchId);
        res.put("size", chain.size());

        if (chain.isEmpty()) {
            res.put("ok", true);
            res.put("msg", "该批次暂无链数据");
            return res;
        }

        String prefix = "0".repeat(DIFFICULTY);

        for (int i = 0; i < chain.size(); i++) {
            TraceRecord cur = chain.get(i);
            TraceRecord prev = (i == 0) ? null : chain.get(i - 1);

            String expectedPrevHash = (prev == null) ? "0" : prev.getBlockHash();
            if (!Objects.equals(cur.getPrevHash(), expectedPrevHash)) {
                return bad(res, i, "prev_hash 不匹配");
            }

            String dataPlain = cur.getBatchId() + "|" + cur.getHeight() + "|" + cur.getType() + "|" +
                    cur.getContent() + "|" + cur.getCreatedAt() + "|" + cur.getPrevHash();
            String expectedDataHash = HashUtil.sha256(dataPlain);
            if (!Objects.equals(cur.getDataHash(), expectedDataHash)) {
                return bad(res, i, "data_hash 不匹配");
            }

            String blockPlain = cur.getPrevHash() + "|" + cur.getDataHash() + "|" + cur.getNonce() + "|" + cur.getHeight();
            String expectedBlockHash = HashUtil.sha256(blockPlain);
            if (!Objects.equals(cur.getBlockHash(), expectedBlockHash)) {
                return bad(res, i, "block_hash 不匹配");
            }

            if (!cur.getBlockHash().startsWith(prefix)) {
                return bad(res, i, "工作量证明不满足难度（前导0不足）");
            }
        }

        res.put("ok", true);
        res.put("msg", "链校验通过，无篡改");
        return res;
    }


    private Map<String, Object> bad(Map<String, Object> res, int index, String reason) {
        res.put("ok", false);
        res.put("badIndex", index);
        res.put("reason", reason);
        return res;
    }
}

