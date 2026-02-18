package org.demo.blockchaintracksystem.controller;

import jakarta.servlet.http.HttpSession;
import org.demo.blockchaintracksystem.entity.Product;
import org.demo.blockchaintracksystem.mapper.ProductMapper;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@CrossOrigin // 你现在走 Vite proxy，其实可要可不要；先留着不动
@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductMapper productMapper;

    public ProductController(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    // 新增产品（从 Session 获取 uid）
    @PostMapping("/create")
    public String create(@RequestParam String name,
                         @RequestParam(required = false) String description,
                         HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) return "未登录";

        if (name == null || name.trim().isEmpty()) return "产品名称不能为空";

        Product p = new Product();
        p.setUserId(uid);
        p.setName(name.trim());
        p.setDescription(description);

        productMapper.insert(p);
        return "创建成功";
    }

    // 查询我的产品列表（从 Session 获取 uid）
    @GetMapping("/my")
    public List<Product> my(HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) return Collections.emptyList();
        return productMapper.selectByUserId(uid);
    }

    // 删除产品（只能删自己的，从 Session 获取 uid）
    @PostMapping("/delete")
    public String delete(@RequestParam Long productId, HttpSession session) {
        Long uid = (Long) session.getAttribute("uid");
        if (uid == null) return "未登录";

        int n = productMapper.deleteByIdAndUserId(productId, uid);
        return n > 0 ? "删除成功" : "删除失败（无权限或不存在）";
    }
}

