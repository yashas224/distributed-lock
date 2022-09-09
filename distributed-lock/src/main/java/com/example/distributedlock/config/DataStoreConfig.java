package com.example.distributedlock.config;

import com.example.distributedlock.lock.Lock;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "lockEntityManagerFactory",
   transactionManagerRef = "lockTransactionManager",
   basePackages = {"com.example.distributedlock.lock"})
public class DataStoreConfig {

  @Value("${lock.datasource.url}")
  String jdbcUrl;

  Logger LOGGER = LoggerFactory.getLogger(DataStoreConfig.class);

  @Primary
  @Bean(name = "lockDataSourceProperties")
  @ConfigurationProperties(prefix = "lock.datasource")
  public DataSourceProperties memberDataSourceProperties() {
    LOGGER.info("JDBC url : {}", jdbcUrl);
    return new DataSourceProperties();
  }

  @Primary
  @Bean(name = "lockDataSource")
  public DataSource dataSource(@Qualifier("lockDataSourceProperties") DataSourceProperties firstDataSourceProperties) {
    return memberDataSourceProperties().initializeDataSourceBuilder()
       .type(HikariDataSource.class).build();
  }

  @Primary
  @Bean(name = "lockEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean
  entityManagerFactory(
     EntityManagerFactoryBuilder builder,
     @Qualifier("lockDataSource") DataSource dataSource
  ) {
    return builder
       .dataSource(dataSource)
       .packages(Lock.class)
       .build();
  }

  @Primary
  @Bean(name = "lockTransactionManager")
  public PlatformTransactionManager transactionManager(
     @Qualifier("lockEntityManagerFactory") LocalContainerEntityManagerFactoryBean lockEntityManagerFactory
  ) {
    return new JpaTransactionManager(lockEntityManagerFactory.getObject());
  }
}