package com.zikozee.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * @author : zikoz
 * @created : 01 Sep, 2021
 */

@Slf4j
@Component
public class HwStepExecutionListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("<><><><><>Before Step Execution, context: {}", stepExecution.getJobExecution().getExecutionContext());
        log.info("Inside step, print job parameters {}", stepExecution.getJobExecution().getJobParameters());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("<><><><><>After Step Execution, context: {}", stepExecution.getJobExecution().getExecutionContext());
        return null;
    }

}
