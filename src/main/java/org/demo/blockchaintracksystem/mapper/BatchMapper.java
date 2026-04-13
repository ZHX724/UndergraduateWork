package org.demo.blockchaintracksystem.mapper;

import org.demo.blockchaintracksystem.entity.Batch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BatchMapper {

    Batch selectByCode(@Param("batchCode") String batchCode);

    int insert(Batch batch);

    List<Batch> selectByProductId(@Param("productId") Long productId);

    int deleteById(@Param("id") Long id);

    Batch selectById(@Param("id") Long id);

    Batch selectByBatchCode(String batchCode);

    List<Batch> selectAll();

    /**
     * 根据产品所属用户的 ID 查询所有批次。
     * @param userId 用户 ID
     * @return 属于该用户的批次列表
     */
    List<Batch> selectByUserId(@Param("userId") Long userId);

    int updateRisk(@Param("id") Long id,
                   @Param("riskFlag") Integer riskFlag,
                   @Param("riskNote") String riskNote);

}