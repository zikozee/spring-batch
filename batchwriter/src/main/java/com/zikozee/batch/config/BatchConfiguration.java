package com.zikozee.batch.config;

import com.zikozee.batch.listener.ProductSkipListener;
import com.zikozee.batch.model.Product;
import com.zikozee.batch.processor.ProductProcessor;
import com.zikozee.batch.tasklet.*;
import com.zikozee.batch.util_package.ColumnRangePartitioner;
import com.zikozee.batch.writer.ConsoleItemWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
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
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
    public FlatFileItemReader reader(@Value("#{jobParameters[fileInput]}") FileSystemResource inputFile) { //we can specify Object type FlatFileItemReader<Product>
        FlatFileItemReader reader = new FlatFileItemReader();
        reader.setResource(inputFile);
        reader.setLinesToSkip(1);

        reader.setLineMapper(new DefaultLineMapper<Product>() {
            {
                setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {
                    {
                        setTargetType(Product.class);
                    }
                });

                setLineTokenizer(new DelimitedLineTokenizer() {
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
    public FlatFileItemWriter<Product> flatFileItemWriter(@Value("#{jobParameters[fileOutput]}") FileSystemResource outputFile) { //we can specify Object type FlatFileItemWriter<Product>

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
        writer.setLineAggregator(new DelimitedLineAggregator<>() {
            {
                setDelimiter(",");
                setFieldExtractor(new BeanWrapperFieldExtractor<>() {
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
    public StaxEventItemWriter xmlWriter(@Value("#{jobParameters[fileOutput]}") FileSystemResource outputFile) {

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
    public JdbcBatchItemWriter dbWriter() {
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
    public JdbcBatchItemWriter dbWriter2() {
        return new JdbcBatchItemWriterBuilder<Product>()
                .dataSource(datasource)
                .sql("insert into batch_product (product_id, prod_name, prod_desc, price, unit) " +
                        " values (:productId, :prodName, :productDesc, :price, :unit)")
                .beanMapped()
                .build();

    }

    @Bean
    public Step step0() {
        return steps.get("step0")
                .tasklet(new ConsoleTasklet())
                .build();
    }

    // when persisting reading or writing to db no need to configure transactionManagement as it is done by default
    @Bean
    public Step step1() {
        return steps.get("step1")
                .<Product, Product>chunk(5)
                .reader(reader(null))
//                .reader(itemReaderServiceAdapter())
                .processor(new ProductProcessor()) // this wa// s not used for the others only testing for flatFile
                .writer(flatFileItemWriter(null))
//                .writer(xmlWriter(null))
//                .writer(dbWriter())
//                .writer(dbWriter2())
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


    /*
     *   ASYNC JOBS
     *
     */


    @Bean
    public Step multiThreadStep() {
//        int cores = Runtime.getRuntime().availableProcessors();
//        System.out.println("cores: " + cores);

        ThreadPoolTaskExecutor threadPoolExecutor = new ThreadPoolTaskExecutor();
        threadPoolExecutor.setCorePoolSize(4);
        threadPoolExecutor.setMaxPoolSize(4);
        threadPoolExecutor.setQueueCapacity(4 * 2);
        threadPoolExecutor.afterPropertiesSet();

        return steps.get("multiThreadStep")
                .<Product, Product>chunk(5)
                .reader(reader(null))

                .processor(new ProductProcessor()) // this wa// s not used for the others only testing for flatFile

                .writer(dbWriter2())

                .taskExecutor(threadPoolExecutor)
                .build();
    }

    /*
     *   ASYNC JOBS
     *
     */

    @Bean
    public AsyncItemProcessor asyncItemProcessor() {

        AsyncItemProcessor processor = new AsyncItemProcessor();
        processor.setDelegate(new ProductProcessor());
        processor.setTaskExecutor(new SimpleAsyncTaskExecutor());// we also use TheadPoolExecutor here
        return processor;
    }

    @StepScope
    @Bean
    public AsyncItemWriter asyncItemWriter() {
        AsyncItemWriter asyncItemWriter = new AsyncItemWriter();
        asyncItemWriter.setDelegate(flatFileItemWriter(null));
        return asyncItemWriter;
    }


    // Async step uses async processing and writing. It also maintains order of Job
    @Bean
    public Step asyncStep() {

        return steps.get("asyncStep")
                .<Product, Product>chunk(5)
                .reader(reader(null))

                .processor(asyncItemProcessor()) // this wa// s not used for the others only testing for flatFile

                .writer(asyncItemWriter())

                .build();
    }

    /*
     *  SPACE
     *
     */


    @Bean
    @StepScope
    public JdbcPagingItemReader pagingItemReader(@Value("#{stepExecutionContext['minValue']}") Long minValue,
                                                 @Value("#{stepExecutionContext['maxValue']}") Long maxValue) {

        Map<String, Order> sortKey = new HashMap<>();
        sortKey.put("product_id", Order.ASCENDING);

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("product_id, prod_name, prod_desc, unit, price");
        queryProvider.setFromClause("from batch_product");
        queryProvider.setWhereClause("where product_id >= " + minValue + " and product_id <" + maxValue);
        queryProvider.setSortKeys(sortKey);


        JdbcPagingItemReader reader = new JdbcPagingItemReader();
        reader.setDataSource(datasource);
        reader.setQueryProvider(queryProvider);
        reader.setFetchSize(1000);

        reader.setRowMapper(new BeanPropertyRowMapper() {
            {
                setMappedClass(Product.class);
            }
        });

        return reader;
    }

    public ColumnRangePartitioner columnRangePartitioner() {
        ColumnRangePartitioner columnRangePartitioner = new ColumnRangePartitioner();
        columnRangePartitioner.setColumn("product_id");
        columnRangePartitioner.setDataSource(datasource);
        columnRangePartitioner.setTable("batch_product");
        return columnRangePartitioner;
    }


    public Step slaveStep() {
        return steps.get("slaveStep")
                .<Product, Product>chunk(5)
                .reader(pagingItemReader(null, null))
                .writer(new ConsoleItemWriter())
                .build();
    }

    public Step partitionStep() {
        return steps.get("partitionStep")
                .partitioner(slaveStep().getName(), columnRangePartitioner()) //use columnRangePartitioner()  :::: More generic
                .step(slaveStep())
                .gridSize(3)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }




    /*
    *
    *
    *       JOBS
    *
    */

    //Steps
//    private final Step downloadStep;
//    private final Step copyFileStep;
//    private final Step pagerDutyStep;
//    private final Step cleanUpStep;
//    private final Flow splitFlow;



//    @Bean
//    public Job normalJob() {
//        return jobs.get("multiThreadJob")
//                .incrementer(new RunIdIncrementer())
//                .start(step0())
//                .next(step1())
//                .build();
//    }
//

    @Bean
    public Job multiThreadJob() {
        return jobs.get("multiThreadJob")
                .incrementer(new RunIdIncrementer())
                .start(step0())
                .next(multiThreadStep())
                .build();
    }


//    @Bean
//    public Job asyncJob() {
//        return jobs.get("asyncJob")
//                .start(step0())
//                .next(asyncStep())
//                .build();
//    }
////
//
//    @Bean
//    public Job parallelJob() {
//        return jobs.get("parallelJob")
//                .start(splitFlow)
//                .next(cleanUpStep)
//                .end()
//                .build();
//    }
//
//    @Bean
//    public Job partitionJob() {
//        return jobs.get("partitionJob")
//                .incrementer(new RunIdIncrementer()) // staring as new instance, necessary for database writing
//                .start(partitionStep())
//                .build();
//    }
//
//
//    @Bean
//    public Job controllerJob() {
//        return jobs.get("controllerJob")
//                .incrementer(new RunIdIncrementer())
//                .start(downloadStep)
//                .on("*").to(copyFileStep)
//                .from(downloadStep).on("FAILED").to(pagerDutyStep).next(cleanUpStep)
//                .end().build();
//    }
}
