package org.demo.blockchaintracksystem.mapper;

import org.demo.blockchaintracksystem.entity.Batch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BatchMapper {

    int insert(Batch batch);

    List<Batch> selectByProductId(@Param("productId") Long productId);

    int deleteById(@Param("id") Long id);

    Batch selectById(@Param("id") Long id);
    Batch selectByBatchCode(String batchCode);

}

