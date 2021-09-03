package com.zikozee.batch.product_adapter;

import com.zikozee.batch.model.Product;
import com.zikozee.batch.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author : zikoz
 * @created : 03 Sep, 2021
 */

//for webservice to read only once instead of infinite loop
@Component
@RequiredArgsConstructor
public class ProductServiceAdapter implements InitializingBean {

    private final ProductService service;
    private List<Product> products;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.products = service.getProduct();
    }

    //this ensures we read  only once
    public Product nextProduct(){
        if(!products.isEmpty())
            return this.products.remove(0);
        else
            return null;
    }

    public ProductService getService() {
        return service;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
