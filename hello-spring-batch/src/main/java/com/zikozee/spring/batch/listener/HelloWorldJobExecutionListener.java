package com.zikozee.spring.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

/**
 * @author : zikoz
 * @created : 01 Sep, 2021
 */
@Slf4j
@Component
public class HelloWorldJobExecutionListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("::::::::Before staring the job {}", jobExecution.getJobInstance().getJobName());
        jobExecution.getExecutionContext().put("my name", "zikozee");
        log.info("::::::::Before staring the job {}", jobExecution.getExecutionContext());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("::::::::After staring the job {}", jobExecution.getExecutionContext());
    }
}
