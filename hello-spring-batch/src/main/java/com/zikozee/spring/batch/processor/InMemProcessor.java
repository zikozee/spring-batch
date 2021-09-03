package com.zikozee.spring.batch.processor;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * @author : zikoz
 * @created : 01 Sep, 2021
 */

@Component
public class InMemProcessor implements ItemProcessor<Integer, Integer> {

    @Override
    public Integer process(Integer item) throws Exception {

        return Integer.sum(10, item);
    }
}
