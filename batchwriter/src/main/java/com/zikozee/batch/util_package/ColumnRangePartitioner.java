package com.zikozee.batch.util_package;

import lombok.Getter;
import lombok.Setter;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : zikoz
 * @created : 11 Sep, 2021
 */

// this is a very generic range partitioner to be used on large tables without worrying about table size
@Getter @Setter
public class ColumnRangePartitioner implements Partitioner {
    private JdbcOperations jdbcTemplate;
    private String table;
    private String column;


    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        int min = this.getJdbcTemplate().queryForObject("select min(" + column + ") from " + table, Integer.class);
        int max = this.getJdbcTemplate().queryForObject("select max(" + column + ") from " + table, Integer.class);

        int targetSize = (max - min)/ gridSize + 1;

        Map<String, ExecutionContext> result= new HashMap<>();
        int number = 0;
        int start = min;
        int end  = start + targetSize -1;

        while (start <= max){
            ExecutionContext value = new ExecutionContext();
            result.put("partition" + number, value);

            if(end >= max){
                end = max;
            }

            value.put("minValue", start);
            value.put("maxValue", end);

            start += targetSize;
            end += targetSize;

            number++;
        }
        return result;
    }
}
