package com.zikozee.batch.config;
;
import com.zikozee.batch.listener.HelloWorldJobExecutionListener;
import com.zikozee.batch.listener.HwStepExecutionListener;
import com.zikozee.batch.model.Product;
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
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

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


    @Bean
    public Step step1(){
        return steps.get("step1")
                .listener(stepExecutionListener)
                .tasklet(helloWorldTasklet())
                .build();
    }


    //we can inject as below or use at value as filed injection and pass arguments set in config
    @StepScope // if we don't set this jobParameter will not be set
    @Bean
    public FlatFileItemReader flatFileItemsReader(@Value("#{jobParameters['fileInput']}") FileSystemResource inputFile){
        FlatFileItemReader reader = new FlatFileItemReader();
        // tep 1 let reader know where the file is
        reader.setResource(inputFile);


        //create the line mapper
        reader.setLineMapper(new DefaultLineMapper<Product>(){
            {
                setLineTokenizer(new DelimitedLineTokenizer(){
                    {
                        setNames("productID", "productName", "productDesc", "price", "unit");
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
    public Step step2(){
        // trying 3 different ways of Instantiating

        return steps.get("step2")
                .<Integer,Integer>chunk(3)
                .reader(flatFileItemsReader(null)) //spring will auto inject here
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
