package org.demo.blockchaintracksystem.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.demo.blockchaintracksystem.entity.Product;

import java.util.List;

@Mapper
public interface ProductMapper {
    int insert(Product p);

    List<Product> selectByUserId(@Param("userId") Long userId);

    int deleteByIdAndUserId(@Param("productId") Long productId,
                            @Param("userId") Long userId);
}
