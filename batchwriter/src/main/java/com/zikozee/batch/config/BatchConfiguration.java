package com.zikozee.batch.config;

import com.zikozee.batch.listener.ProductSkipListener;
import com.zikozee.batch.model.Product;
import com.zikozee.batch.processor.ProductProcessor;
import com.zikozee.batch.product_adapter.ProductServiceAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.*;
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
import org.springframework.web.client.ResourceAccessException;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    private final DataSource datasource;
    private final ProductSkipListener productSkipListener;

//    private final ProductServiceAdapter serviceAdapter;

    /*
    public ItemReaderAdapter itemReaderServiceAdapter(){
        ItemReaderAdapter readerAdapter = new ItemReaderAdapter();
        readerAdapter.setTargetObject(serviceAdapter);
        readerAdapter.setTargetMethod("nextProduct");
        return readerAdapter;
    }

     */

    @Bean
    @StepScope
    public FlatFileItemReader reader(@Value("#{jobParameters[fileInput]}") FileSystemResource inputFile){ //we can specify Object type FlatFileItemReader<Product>
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
    public FlatFileItemWriter<Product> writer(@Value("#{jobParameters[fileOutput]}") FileSystemResource outputFile){ //we can specify Object type FlatFileItemWriter<Product>

        /*
        FlatFileItemWriter writer = new FlatFileItemWriter<Product>(){
            //for testing skip writing
            @Override
            public String doWrite(List<? extends Product> items) {
                for(Product item: items){
                    if (item.getProductId() == 9)
                        throw new RuntimeException("Because ID is 9");

                }
                return super.doWrite(items);
            }
        };

         */
        FlatFileItemWriter writer = new FlatFileItemWriter<Product>();
        writer.setResource(outputFile);
        writer.setLineAggregator(new DelimitedLineAggregator<>(){
            {
                setDelimiter(",");
                setFieldExtractor(new BeanWrapperFieldExtractor<>(){
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

        writer.setAppendAllowed(true); // appending to already existing record

//        writer.setFooterCallback(new FlatFileFooterCallback() {
//            @Override
//            public void writeFooter(Writer writer) throws IOException {
//                writer.write("This file was created at: "  + new SimpleDateFormat().format(new Date()));
//            }
//        });

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
    @StepScope
    public JdbcBatchItemWriter dbWriter(){
        JdbcBatchItemWriter writer = new JdbcBatchItemWriter();
        writer.setDataSource(datasource);
        writer.setSql("insert into batch_product (product_id, prod_name, prod_desc, price, unit) " +
                " values (?, ?, ?, ?, ?)");
        writer.setItemPreparedStatementSetter(new ItemPreparedStatementSetter<Product>() {
            @Override
            public void setValues(Product item, PreparedStatement ps) throws SQLException {
                ps.setInt(1, item.getProductId());
                ps.setString(2, item.getProdName());
                ps.setString(3, item.getProductDesc());
                ps.setBigDecimal(4, item.getPrice());
                ps.setInt(5, item.getUnit());
            }
        });

        return writer;
    }

    //alternative in case we have multiple prepared statements
    // we use field names directly like jpql
    @Bean
    public JdbcBatchItemWriter dbWriter2(){
        return new JdbcBatchItemWriterBuilder<Product>()
                .dataSource(datasource)
                .sql("insert into batch_product (product_id, prod_name, prod_desc, price, unit) " +
                        " values (:productId, :prodName, :productDesc, :price, :unit)")
                .beanMapped()
                .build();

    }

    @Bean
    public Step step0(){
        return steps.get("step0")
                .tasklet(new ConsoleTasklet())
                .build();
    }

    // when persisting reading or writing to db no need to configure transactionManagement as it is done by default
    @Bean
    public Step step1(){
        return steps.get("step1")
                .<Product, Product>chunk(3)
                .reader(reader(null))
//                .reader(itemReaderServiceAdapter())
                .processor(new ProductProcessor()) // this wa// s not used for the others only testing for flatFile
//                .writer(writer(null))
//                .writer(xmlWriter(null))
//                .writer(dbWriter())
                .writer(dbWriter2())
                .faultTolerant()
//                .retry(ResourceAccessException.class)
//                .retryLimit(5)
                .skip(FlatFileParseException.class)
//                .skip(FlatFileParseException.class)
                .skipLimit(3) //total error it can skip before throwing exception OR JUST USE SKIP POLICY
                //.skipPolicy(new AlwaysSkipItemSkipPolicy()) // this skips all error in read, process and write use with understanding
                //.listener(productSkipListener)
                .build();
    }

    @Bean
    public Job job1(){
        return jobs.get("job1")
                .incrementer(new RunIdIncrementer()) // staring as new instance, necessary for database writing
                .start(step0())
                .next(step1())
                .build();

    }
}
