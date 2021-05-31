package com.rostelecom.jirasync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.rostelecom")
public class JiraSyncApplication {
    public static void main(String[] args) {
        SpringApplication.run(JiraSyncApplication.class, args);
    }
}
