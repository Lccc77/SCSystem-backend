package com.scsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.scsystem.mapper")
@SpringBootApplication
public class SCSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SCSystemApplication.class, args);
    }

}
