package com.zikozee.batch.service;

import com.zikozee.batch.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author : zikoz
 * @created : 03 Sep, 2021
 */


@RequiredArgsConstructor
public class ProductService {

    public Product getProduct(){
        String url = "http://localhost:8080/product";
        RestTemplate restTemplate = new RestTemplate();
        Product product = restTemplate.getForObject(url, Product.class);

        return product;
    }
}
