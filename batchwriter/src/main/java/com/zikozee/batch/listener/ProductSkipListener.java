package com.zikozee.batch.listener;

import org.springframework.batch.core.annotation.OnSkipInProcess;
import org.springframework.batch.core.annotation.OnSkipInRead;
import org.springframework.batch.core.annotation.OnSkipInWrite;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author : zikoz
 * @created : 06 Sep, 2021
 */

@Component
public class ProductSkipListener {

    public static final String READ_ERR_FILENAME ="batchwriter/error/read_skipped";
    private static final String PROCESS_ERR_FILENAME ="batchwriter/error/process_skipped";
    private static final String WRITE_ERR_FILENAME ="batchwriter/error/write_skipped";

    @OnSkipInRead  // we also have OnskipInProcess,  OnskipInWrite
    public void onSkipRead(Throwable throwable){
        if(throwable instanceof FlatFileParseException){
            FlatFileParseException fileParseException = (FlatFileParseException) throwable;  // we can get input and line number, check FlatFileParseException implementations
            onSKipWriter(fileParseException.getInput(), READ_ERR_FILENAME);
        }
    }

    @OnSkipInProcess
    public void onSkipProcessing(Object item, Throwable throwable){
        if(throwable instanceof RuntimeException){
            onSKipWriter(item, PROCESS_ERR_FILENAME);
        }
    }

    @OnSkipInWrite
    public void onSkipWriting(Object item, Throwable throwable){
        if(throwable instanceof RuntimeException){
            onSKipWriter(item, WRITE_ERR_FILENAME);
        }
    }

    public void onSKipWriter(Object object, String fname){

        // We can write to the database, where some can go in fix the errors and resend to queue
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(fname, true);
            fos.write(object.toString().getBytes(StandardCharsets.UTF_8));
            fos.write("\r\n".getBytes(StandardCharsets.UTF_8));
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
