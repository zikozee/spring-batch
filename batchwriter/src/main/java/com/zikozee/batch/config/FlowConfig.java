package com.zikozee.batch.config;

import com.zikozee.batch.tasklet.*;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 * @author : zikoz
 * @created : 11 Sep, 2021
 */

@Configuration
@RequiredArgsConstructor
public class FlowConfig {

    private final StepBuilderFactory steps;


    @Bean
    public Step downloadStep(){
        return steps.get("downloadStep")
                .tasklet(new DownloadTasklet())
                .build();
    }

    @Bean
    public Step fileProcessStep(){
        return steps.get("fileProcessStep")
                .tasklet(new FileProcessTasklet())
                .build();
    }

    @Bean
    public Step bizStep3(){
        return steps.get("bizStep3")
                .tasklet(new BizTasklet3())
                .build();
    }

    @Bean
    public Step bizStep4(){
        return steps.get("bizStep4")
                .tasklet(new BizTasklet4())
                .build();
    }

    @Bean
    public Step cleanUpStep(){
        return steps.get("cleanUpStep")
                .tasklet(new CleanupTasklet())
                .build();
    }


    public Flow fileFlow(){
        return new FlowBuilder< SimpleFlow >("fileFlow")
                .start(downloadStep())
                .next(fileProcessStep())
                .build();
    }

    public Flow bizFlow1(){
        return new FlowBuilder< SimpleFlow >("bizFlow1")
                .start(bizStep3())
                .build();
    }

    @Bean
    public Step pagerDutyStep(){
        return steps.get("pagerDutyStep")
                .tasklet(new PagerDutyTaskLet())
                .build();
    }

    public Flow bizFlow2(){
        return new FlowBuilder<SimpleFlow>("bizFlow2")
                .start(bizStep4())

                //todo info: Controlling Job flow start
                .from(bizStep4()).on("*").end()  //pass on everything else
                .on("FAILED")
                .to(pagerDutyStep())
                //todo info: Controlling Job flow end

                .build();
    }

    @Bean
    public Flow splitFlow(){
        return new FlowBuilder<SimpleFlow>("splitFlow")
                .split(new SimpleAsyncTaskExecutor())
                .add(fileFlow(), bizFlow1(), bizFlow2())
                .build();

    }
}


