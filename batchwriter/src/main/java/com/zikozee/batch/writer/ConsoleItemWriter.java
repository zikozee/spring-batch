package com.zikozee.batch.writer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.support.AbstractItemStreamItemWriter;

import java.util.List;

/**
 * @author : zikoz
 * @created : 01 Sep, 2021
 */

@Slf4j
public class ConsoleItemWriter extends AbstractItemStreamItemWriter {

    @Override
    public void write(List items) throws Exception {
        items.forEach(System.out::println);
        System.out.println(" ******** writing each chunk ********");
    }
}
