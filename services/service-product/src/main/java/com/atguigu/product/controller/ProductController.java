package com.atguigu.product.controller;

import com.atguigu.product.bean.Product;
import com.atguigu.product.service.ProductService;
import com.sun.net.httpserver.HttpsServer;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.http.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



/*@RequestMapping("/api/product")*/
@RestController
public class ProductController {
    @Autowired
    ProductService productService;
    @GetMapping("/product/{id}")
    public Product getProduct(@PathVariable("id") Long productId,
                               HttpServletRequest request){
        String header = request.getHeader("X-Token");
        System.out.println("hello.....token=["+ header+"]");
        Product product =productService.getProductById(productId);
        return product;
    }
}
