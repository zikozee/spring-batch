package com.zikozee.batch.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * @author : zikoz
 * @created : 11 Sep, 2021
 */

@Slf4j
public class FileProcessTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        log.info("File process tasks started");
        Thread.sleep(1000);
        log.info("File process tasks completed\n");
        return RepeatStatus.FINISHED;
    }
}
