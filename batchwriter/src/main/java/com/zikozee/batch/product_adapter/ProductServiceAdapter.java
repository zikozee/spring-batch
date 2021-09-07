package com.zikozee.batch.product_adapter;

import com.zikozee.batch.model.Product;
import com.zikozee.batch.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author : zikoz
 * @created : 03 Sep, 2021
 */


@Slf4j

@RequiredArgsConstructor
public class ProductServiceAdapter {

    private final ProductService service;


    public Product nextProduct() throws InterruptedException {
        Product product = null;
        Thread.sleep(1000);
        try{
            product = service.getProduct();
            log.info("connected to web service .... ok");
        }catch (Exception e){
            log.info("exception ..." + e.getMessage());
            throw  e;
        }
        return product;
    }

}
