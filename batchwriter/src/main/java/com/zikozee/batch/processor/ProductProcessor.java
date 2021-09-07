package com.zikozee.batch.processor;

import com.zikozee.batch.model.Product;
import org.springframework.batch.item.ItemProcessor;

/**
 * @author : zikoz
 * @created : 04 Sep, 2021
 */

public class ProductProcessor implements ItemProcessor<Product, Product> {
    // we can receive an object and return an entirely different object e.g <Users, Sales>
    // we can carry out custom complex business logic here
    @Override
    public Product process(Product item) throws Exception {
//        if(item.getProductId() == 2) throw new RuntimeException("Because ID is 2"); // simulation to test skip Processing error
//        else
            item.setProductDesc(item.getProductDesc().toUpperCase());

        return item;
    }
}
