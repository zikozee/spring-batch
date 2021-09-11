package com.zikozee.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BatchwriterApplication {

    public static void main(String[] args) {
        //to ensure it finished System.exit(SpringApplication.exit(...))
      System.exit(SpringApplication.exit(SpringApplication.run(BatchwriterApplication.class, args)));
    }

}
