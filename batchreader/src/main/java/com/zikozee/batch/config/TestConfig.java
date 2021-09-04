package com.zikozee.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author : zikoz
 * @created : 04 Sep, 2021
 */

@Configuration
@Slf4j
public class TestConfig {

    @Bean
    @Qualifier("bean1")
    public RestTemplate restTemplate1(){
        log.info("::::first rest Template::::");
        return new RestTemplate();
    }

    @Bean
    @Qualifier("bean2")
    public RestTemplate restTemplate2(){
        log.info("second rest Template");
        return new RestTemplate();
    }

}
