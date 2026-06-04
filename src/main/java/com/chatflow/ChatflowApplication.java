package com.chatflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ChatflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatflowApplication.class, args);
    }
}
