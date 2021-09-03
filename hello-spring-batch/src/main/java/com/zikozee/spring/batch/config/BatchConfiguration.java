package com.zikozee.spring.batch.config;

import com.zikozee.spring.batch.listener.HelloWorldJobExecutionListener;
import com.zikozee.spring.batch.listener.HwStepExecutionListener;
import com.zikozee.spring.batch.processor.InMemProcessor;
import com.zikozee.spring.batch.reader.InMemReader;
import com.zikozee.spring.batch.writer.ConsoleItemWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    private final InMemProcessor processor;


    @Bean
    public Step step1(){
        return steps.get("step1")
                .listener(stepExecutionListener)
                .tasklet(helloWorldTasklet())
                .build();
    }

    @Bean
    public InMemReader reader(){
        return new InMemReader();
    }

    @Bean
    public Step step2(){
        // trying 3 different ways of Instantiating

        return steps.get("step2")
                .<Integer,Integer>chunk(3)
                .reader(reader())
                .processor(processor)
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
