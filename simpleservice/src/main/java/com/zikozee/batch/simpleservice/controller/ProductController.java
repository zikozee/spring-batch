package com.zikozee.batch.simpleservice.controller;

import com.zikozee.batch.simpleservice.model.Product;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * @author : zikoz
 * @created : 03 Sep, 2021
 */

@RestController
public class ProductController {

    @GetMapping(path = "products")
    public ResponseEntity<List<Product>> getProduct(){
        Product product1 = Product.builder()
                .productId(1)
                .prodName("Apple")
                .productDesc("Apple from webservice")
                .price(BigDecimal.valueOf(325.55))
                .unit(10)
                .build();

        Product product2 = Product.builder()
                .productId(2)
                .prodName("Dell")
                .productDesc("Dell from webservice")
                .price(BigDecimal.valueOf(625.66))
                .unit(30)
                .build();

        return new ResponseEntity<>(Arrays.asList(product1, product2), HttpStatus.OK);
    }
}
