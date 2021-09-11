package com.zikozee.batch.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * @author : zikoz
 * @created : 11 Sep, 2021
 */

@Slf4j
public class BizTasklet4 implements Tasklet, StepExecutionListener {
    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        log.info("Business Task 4 started");
        Thread.sleep(1000);
        log.info("Business Task 4 Completed\n");
        return RepeatStatus.FINISHED;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {

    }

    //todo info: For demo-ing Job Flow Control
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        long randomNumber = Math.round( Math.random() );
        log.info("Random number generated: {}", randomNumber);
        if(randomNumber ==1 ){
            log.info("Tasklet passed");
            return ExitStatus.COMPLETED;
        }else {
            log.info("Tasklet failed");
            return ExitStatus.FAILED;
        }

    }
}
