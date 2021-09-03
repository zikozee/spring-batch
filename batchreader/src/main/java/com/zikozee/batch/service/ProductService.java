package com.zikozee.batch.service;

import com.zikozee.batch.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : zikoz
 * @created : 03 Sep, 2021
 */

@Service
public class ProductService {

    public List<Product> getProduct(){
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/products";
        Product[] products = restTemplate.getForObject(url, Product[].class);

        return Arrays.stream(products).collect(Collectors.toList());
    }
}
