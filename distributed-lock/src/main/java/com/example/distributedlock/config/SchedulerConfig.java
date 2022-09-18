package com.example.distributedlock.config;

import Constants.Constants;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "120s")
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class SchedulerConfig {
  Logger LOGGER = LoggerFactory.getLogger(SchedulerConfig.class);

  @Value("${spring.application.name}")
  private String appName;

  @Bean
  public LockProvider lockProvider(DataSource dataSource) {
    return new JdbcTemplateLockProvider(
       JdbcTemplateLockProvider.Configuration.builder()
          .withJdbcTemplate(new JdbcTemplate(dataSource))
          .usingDbTime()
          .build()
    );
  }

  @Bean
  public TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(3);
    threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
    return threadPoolTaskScheduler;
  }

  @Scheduled(cron = "0 0/2 * * * ?")
  public void schedulingJob() throws UnknownHostException {
    InetAddress ip = InetAddress.getLocalHost();
    String hostname = ip.getHostName();
    LOGGER.info("Started Normal scheduling job from host {}", hostname);
  }

  @Scheduled(cron = "0 0/2 * * * ?")
  @SchedulerLock(name = Constants.schedulerName, lockAtLeastFor = "120s")
  public void schedulingJobWithShedLock() throws UnknownHostException {
    LockAssert.assertLocked();

    InetAddress ip = InetAddress.getLocalHost();
    String hostname = ip.getHostName();
    LOGGER.info("Started Locked scheduling job from host {}", hostname);
  }
}
