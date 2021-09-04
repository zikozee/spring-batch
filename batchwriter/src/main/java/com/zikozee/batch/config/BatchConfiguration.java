package com.zikozee.batch.config;

import com.zikozee.batch.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
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

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
    private final DataSource datasource;


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
    public Step step1(){
        return steps.get("step1")
                .<Product, Product>chunk(3)
                .reader(reader(null))
//                .writer(writer(null))
//                .writer(xmlWriter(null))
//                .writer(dbWriter())
                .writer(dbWriter2())
                .build();
    }

    @Bean
    public Job job1(){
        return jobs.get("job1")
                .incrementer(new RunIdIncrementer()) // necessary for database writing
                .start(step1())
                .build();

    }
}
