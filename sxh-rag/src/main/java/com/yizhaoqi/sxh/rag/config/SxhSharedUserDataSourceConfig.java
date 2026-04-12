package com.yizhaoqi.sxh.rag.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class SxhSharedUserDataSourceConfig {

    @Bean(name = "sxhUserDataSourceProperties")
    @ConfigurationProperties("sxh.user.datasource")
    public DataSourceProperties sxhUserDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "sxhUserDataSource")
    @ConfigurationProperties("sxh.user.datasource.hikari")
    public DataSource sxhUserDataSource(
            @Qualifier("sxhUserDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    @Bean(name = "sxhUserJdbcTemplate")
    public JdbcTemplate sxhUserJdbcTemplate(@Qualifier("sxhUserDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
