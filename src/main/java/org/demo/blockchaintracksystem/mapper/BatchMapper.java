package org.demo.blockchaintracksystem.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.demo.blockchaintracksystem.entity.Batch;

import java.util.List;

@Mapper
public interface BatchMapper {

    int insert(Batch b);

    // 只查某个产品的批次
    List<Batch> selectByProductId(@Param("productId") Long productId);

    // 删除批次：只能删“自己名下的产品”的批次（通过 join product 来限制）
    int deleteByIdAndUid(@Param("batchId") Long batchId, @Param("uid") Long uid);

    // 校验这个 productId 是否属于当前 uid（用于创建/查询的权限判断）
    int countProductOwnedByUid(@Param("productId") Long productId, @Param("uid") Long uid);
}
