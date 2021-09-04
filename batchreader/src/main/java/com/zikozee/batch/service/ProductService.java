package com.zikozee.batch.service;

import com.zikozee.batch.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
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
@RequiredArgsConstructor
public class ProductService {

    @Qualifier("bean2") // you must use lombok.config and add config as shown for this to work
    private final RestTemplate restTemplate;

    public List<Product> getProduct(){
//        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/products";
        Product[] products = restTemplate.getForObject(url, Product[].class);

        return Arrays.stream(products).collect(Collectors.toList());
    }
}
