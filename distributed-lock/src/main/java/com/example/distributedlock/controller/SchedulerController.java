package com.example.distributedlock.controller;

import Constants.Constants;
import com.example.distributedlock.config.SchedulerConfig;
import com.example.distributedlock.lock.Lock;
import com.example.distributedlock.lock.LockRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Set;

@RestController
@RequestMapping("/scheduler")
public class SchedulerController {
  private static final String SCHEDULED_TASKS = "scheduledTasks";
  @Value("${spring.application.name}")
  private String appName;
  @Autowired
  private ScheduledAnnotationBeanPostProcessor postProcessor;
  Logger LOGGER = LoggerFactory.getLogger(SchedulerController.class);

  @Autowired
  LockRepository lockRepository;
  @Autowired
  private SchedulerConfig schedulerConfig;

  @GetMapping(value = "/stop")
  public String stopSchedule() {
    postProcessor.postProcessBeforeDestruction(schedulerConfig, SCHEDULED_TASKS);
    return "STOPPED";
  }

  @GetMapping(value = "/start")
  public String startSchedule() {
    postProcessor.postProcessAfterInitialization(schedulerConfig, SCHEDULED_TASKS);
    return "STARTED";
  }

  @GetMapping(value = "/list")
  public String listSchedules() throws JsonProcessingException {
    Set<ScheduledTask> setTasks = postProcessor.getScheduledTasks();
    if(!setTasks.isEmpty()) {
      return setTasks.toString();
    } else {
      return "Currently no scheduler tasks are running";
    }
  }

  // stop scheduling
  // for multiple instance deployed applications
  // set the lock until to a duration in the future
  // so that the lock is never released by a instance.
  @PutMapping("/stop-all-instances/{duration}")
  public String stopSchedulerUntilTime(@PathVariable(name = "duration") int duration) {
    Lock lockObject = lockRepository.findByName(Constants.schedulerName);
    Timestamp currentLockUntilTime = lockObject.getLockUntil();
    currentLockUntilTime.setTime(currentLockUntilTime.getTime() + duration * 60 * 1000);
    lockRepository.save(lockObject);
    LOGGER.info("STOPPED SCHEDULING !!");
    return "STOPPED SCHEDULING !!";
  }

  // start scheduling
  // set time back to the current time
  // so that any instances available can acquire the lock
  @PutMapping("/start-all-instances")
  public String startScheduler(@PathVariable(name = "duration") int duration) {
    Lock lockObject = lockRepository.findByName(Constants.schedulerName);
    lockObject.getLockUntil().setTime(System.currentTimeMillis());
    lockRepository.save(lockObject);
    LOGGER.info("STARTED  SCHEDULING AGAIN !!!");
    return "STARTED  SCHEDULING AGAIN !!!";
  }

  @GetMapping("/health")
  public String heclthCheck() throws UnknownHostException {
    InetAddress ip = InetAddress.getLocalHost();
    String hostname = ip.getHostName();
    LOGGER.info("successfully running ".concat(appName).concat("from IP:").concat(hostname));
    return "successfully running ".concat(appName).concat("from IP:").concat(hostname);
  }
}