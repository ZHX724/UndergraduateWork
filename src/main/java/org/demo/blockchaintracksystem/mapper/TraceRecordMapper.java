package org.demo.blockchaintracksystem.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.demo.blockchaintracksystem.entity.TraceRecord;

import java.util.List;

@Mapper
public interface TraceRecordMapper {

    int insert(TraceRecord r);

    // ✅ verify 用：查询该 batch 的整条链（按 height 从小到大）
    List<TraceRecord> selectByBatchIdOrderByHeight(@Param("batchId") Long batchId);

    // ✅ 新增区块用：查询最后一个区块（最大 height）
    TraceRecord selectLastByBatchId(@Param("batchId") Long batchId);
}

