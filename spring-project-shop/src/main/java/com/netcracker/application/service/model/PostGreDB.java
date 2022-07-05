package com.netcracker.application.service.model;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class PostGreDB {
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver definition exception");
            e.printStackTrace();
        }
    }

    @Bean
    public static DataSource dataSource() {
        PGSimpleDataSource PGdataSource = new PGSimpleDataSource();
        PGdataSource.setUrl("jdbc:postgresql:Shop");
        PGdataSource.setUser("test");
        PGdataSource.setPassword("test");
        return PGdataSource;
    }
}
