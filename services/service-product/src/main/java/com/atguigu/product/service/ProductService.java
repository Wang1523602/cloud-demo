package com.atguigu.product.service;

import com.atguigu.product.bean.Product;
import org.springframework.stereotype.Service;



public interface ProductService {


    Product getProductById(Long productId);
}
