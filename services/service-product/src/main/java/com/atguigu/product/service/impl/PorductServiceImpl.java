package com.atguigu.product.service.impl;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import com.atguigu.product.bean.Product;
import com.atguigu.product.service.ProductService;
import org.springframework.stereotype.Service;


@Service
public class PorductServiceImpl implements ProductService {
    @Override
    public Product getProductById(Long productId) {
        Product product = new Product();
        product.setId(productId);
        product.setPrice(new BigDecimal("99"));
        product.setProductName("苹果"+productId);
        product.setNum(2);

  // try {
  //      /* 1. 作用：让【当前线程】暂停执行，进入休眠状态
  //          2. 单位：SECONDS → 秒，所以这里是 休眠100秒
  //          3. 优点：比 Thread.sleep(100000) 可读性更强，语义清晰
  //          4. 底层：本质还是调用了 Thread.sleep(毫秒数)
  //                */
  //         TimeUnit.SECONDS.sleep(100);
  //     } catch (InterruptedException e) {
  //         throw new RuntimeException(e);
  //     }
       return product;
    }
}