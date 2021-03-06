package com.zikozee.batch.config;
;
import com.zikozee.batch.listener.HelloWorldJobExecutionListener;
import com.zikozee.batch.listener.HwStepExecutionListener;
import com.zikozee.batch.model.Product;
import com.zikozee.batch.product_adapter.ProductServiceAdapter;
import com.zikozee.batch.service.ProductService;
import com.zikozee.batch.writer.ConsoleItemWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.sql.DataSource;

/**
 * @author : zikoz
 * @created : 01 Sep, 2021
 */
@Configuration
@EnableBatchProcessing
@Slf4j
@RequiredArgsConstructor
public class BatchConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final HelloWorldJobExecutionListener jobExecutionListener;
    private final HwStepExecutionListener stepExecutionListener;
    private final DataSource dataSource;
    private final ProductServiceAdapter adapter;



    @Bean
    public Step step1(){
        return steps.get("step1")
                .listener(stepExecutionListener)
                .tasklet(helloWorldTasklet())
                .build();
    }


    @StepScope
    @Bean
    public StaxEventItemReader xmlItemReader(@Value("#{jobParameters['fileInput']}") FileSystemResource inputFile){
        //where to read xml from
        StaxEventItemReader reader = new StaxEventItemReader();

        reader.setResource(inputFile);

        //set xml element root: the tag that described the domain object
        reader.setFragmentRootElementName("product");

        // tell reader how to parse xml and which domain object to be passed
        reader.setUnmarshaller(new Jaxb2Marshaller(){
            {
                setClassesToBeBound(Product.class);
            }
        });

        return reader;

    }


    //we can inject as below or use at value as filed injection and pass arguments set in config
    @StepScope // if we don't set this jobParameter will not be set
    @Bean
    public FlatFileItemReader flatFileItemsReader(@Value("#{jobParameters['fileInput']}") FileSystemResource inputFile){
        FlatFileItemReader reader = new FlatFileItemReader();
        // step 1 let reader know where the file is
        reader.setResource(inputFile);


        //step 2 create the line mapper
        reader.setLineMapper(new DefaultLineMapper<Product>(){
            {
                setLineTokenizer(new DelimitedLineTokenizer(){
                    {
                        setNames("productID", "productName", "productDesc", "price", "unit");
                        setDelimiter("|");
                    }
                });

                setFieldSetMapper(new BeanWrapperFieldSetMapper<>(){
                    {
                        setTargetType(Product.class);
                    }
                });
            }
        });

        //step 3 tell reader to skip header
        reader.setLinesToSkip(1);
        return reader;
    }


    @StepScope // if we don't set this jobParameter will not be set
    @Bean
    public FlatFileItemReader flatFileFixItemsReader(@Value("#{jobParameters['fileInput']}") FileSystemResource inputFile){
        FlatFileItemReader reader = new FlatFileItemReader();
        // step 1 let reader know where the file is
        reader.setResource(inputFile);


        //step 2 create the line mapper
        reader.setLineMapper(new DefaultLineMapper<Product>(){
            {
                setLineTokenizer(new FixedLengthTokenizer(){// difference
                    {
                        setNames("productID", "productName", "productDesc", "price", "unit");
                        setColumns(new Range(1, 16),
                                new Range(17, 41),
                                new Range(42, 65),
                                new Range(66, 73),
                                new Range(74, 80));
                    }
                });

                setFieldSetMapper(new BeanWrapperFieldSetMapper<>(){
                    {
                        setTargetType(Product.class);
                    }
                });
            }
        });

        //step 3 tell reader to skip header
        reader.setLinesToSkip(1);
        return reader;
    }


    @Bean
    public JdbcCursorItemReader jdbcCursorItemReader(){
        JdbcCursorItemReader reader = new JdbcCursorItemReader();
        //1. where to read from
        reader.setDataSource(dataSource);
        //2. set sql
        reader.setSql("select product_id, prod_name, prod_desc as productDesc, unit, price from batch_product");
        //3. map to bean
        reader.setRowMapper(new BeanPropertyRowMapper<Product>(){
            {
                setMappedClass(Product.class);
            }
        });

        return reader;
    }

    @StepScope
    @Bean
    public JsonItemReader jsonItemReader(@Value("#{jobParameters['fileInput']}") FileSystemResource inputFile){

        JsonItemReader reader = new JsonItemReader(inputFile, new JacksonJsonObjectReader(Product.class));
        return reader;

    }


    @Bean
    public ItemReaderAdapter serviceItemReader(){
        ItemReaderAdapter reader = new ItemReaderAdapter();

        // todo warning: this return infinite loop
//        reader.setTargetObject(productService);
//        reader.setTargetMethod("getProduct");

        //todo info: this ensures batch reads only once by returning null at some point i.e the adapter
        reader.setTargetObject(adapter);
        reader.setTargetMethod("nextProduct");

        return reader;

    }

    @Bean
    public Step step2(){
        // trying 3 different ways of Instantiating

        return steps.get("step2")
                .<Integer,Integer>chunk(3)
//                .reader(flatFileItemsReader(null)) //spring will auto-inject here
//                .reader(xmlItemReader(null))
//                .reader(flatFileFixItemsReader(null))
//                .reader(jdbcCursorItemReader())
//                .reader(jsonItemReader(null))
                .reader(serviceItemReader())
                .writer(new ConsoleItemWriter())
                .build();
    }

    private Tasklet helloWorldTasklet() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                log.info("Job log: ==>> hello world");
                return RepeatStatus.FINISHED;
            }
        };
    }

    @Bean
    public Job helloWorldJob(){
        return jobs.get("helloworldJob")
                .incrementer(new RunIdIncrementer()) // each time i want to run with a new id
                .listener(jobExecutionListener)
                .start(step1())
                .next(step2())
                .build();
    }
}
