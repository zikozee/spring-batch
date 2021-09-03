package com.zikozee.spring.batch.reader;

import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author : zikoz
 * @created : 01 Sep, 2021
 */

public class InMemReader extends AbstractItemStreamItemReader {

    List<Integer> intList= List.of(1,2,3,4,5,6,7,8,9,10);

    int index = 0;

    @Override
    public Object read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        Integer nextItem = null;

        if(index < intList.size()){
            nextItem = intList.get(index);
            index++;
        }else{
            index = 0;
        }
        return nextItem;
    }
}
