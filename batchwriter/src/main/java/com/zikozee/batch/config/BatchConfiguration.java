package com.zikozee.batch.config;

import com.zikozee.batch.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

        // how to write the header
        writer.setHeaderCallback(new FlatFileHeaderCallback() {
            @Override
            public void writeHeader(Writer writer) throws IOException {
                writer.write("productId, prodName, productDesc, price, unit");
            }
        });

        writer.setAppendAllowed(true);

        writer.setFooterCallback(new FlatFileFooterCallback() {
            @Override
            public void writeFooter(Writer writer) throws IOException {
                writer.write("This file was created at: "  + new SimpleDateFormat().format(new Date()));
            }
        });

        return writer;
    }

    @Bean
    @StepScope
    public StaxEventItemWriter xmlWriter(@Value("#{jobParameters[fileOutput]}") FileSystemResource outputFile){

        XStreamMarshaller marshaller = new XStreamMarshaller();
        Map<String, Class> aliases = new HashMap<>();
        aliases.put("Product", Product.class);
        marshaller.setAliases(aliases);  // this ensures xml class name is not a package name
        marshaller.setAutodetectAnnotations(true); // this ensures Xstream on field name mapping works

        StaxEventItemWriter staxEventItemWriter = new StaxEventItemWriter();

        staxEventItemWriter.setResource(outputFile);
        staxEventItemWriter.setMarshaller(marshaller);
        staxEventItemWriter.setRootTagName("Products"); //<Products> <Product></Product> </Products>

        return staxEventItemWriter;
    }

    @Bean
    public Step step1(){
        return steps.get("step1")
                .<Product, Product>chunk(3)
                .reader(reader(null))
//                .writer(writer(null))
                .writer(xmlWriter(null))
                .build();
    }

    @Bean
    public Job job1(){
        return jobs.get("job1")
                .start(step1())
                .build();

    }
}
