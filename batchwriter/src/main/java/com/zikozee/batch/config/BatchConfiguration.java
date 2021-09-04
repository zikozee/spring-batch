package com.zikozee.batch.config;

import com.zikozee.batch.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

/**
 * @author : zikoz
 * @created : 04 Sep, 2021
 */

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfiguration {

    private final StepBuilderFactory steps;
    private final JobBuilderFactory jobs;


    @Bean
    @StepScope
    public FlatFileItemReader reader(@Value("#{jobParameters[fileInput]}") FileSystemResource inputFile){
        FlatFileItemReader reader = new FlatFileItemReader();
        reader.setResource(inputFile);
        reader.setLinesToSkip(1);

        reader.setLineMapper(new DefaultLineMapper<Product>(){
            {
                setFieldSetMapper(new BeanWrapperFieldSetMapper<>(){
                    {
                        setTargetType(Product.class);
                    }
                });

                setLineTokenizer(new DelimitedLineTokenizer(){
                    {
                        setNames("productID", "productName", "productDesc", "price", "unit");
                    }
                });
            }
        });

        return reader;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter writer(@Value("#{jobParameters[fileOutput]}") FileSystemResource outputFile){

        FlatFileItemWriter writer = new FlatFileItemWriter();
        writer.setResource(outputFile);
        writer.setLineAggregator(new DelimitedLineAggregator(){
            {
                setDelimiter(",");
                setFieldExtractor(new BeanWrapperFieldExtractor(){
                    {
                        setNames(new String[]{"productId", "prodName", "productDesc", "price", "unit"});
                    }
                });
            }
        });

        return writer;
    }

    @Bean
    public Step step1(){
        return steps.get("step1")
                .<Product, Product>chunk(3)
                .reader(reader(null))
                .writer(writer(null))
                .build();
    }

    @Bean
    public Job job1(){
        return jobs.get("job1")
                .start(step1())
                .build();

    }
}
