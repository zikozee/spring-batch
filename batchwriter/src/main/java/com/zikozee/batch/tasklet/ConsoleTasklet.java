package com.zikozee.batch.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * @author : zikoz
 * @created : 07 Sep, 2021
 */

@Slf4j
public class ConsoleTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        log.info("step 0" + "print on console  *******************    ");
        return RepeatStatus.FINISHED;
    }
}
