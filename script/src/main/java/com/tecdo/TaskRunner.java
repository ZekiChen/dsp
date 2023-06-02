package com.tecdo;

import com.tecdo.core.launch.thread.ThreadPool;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class TaskRunner implements CommandLineRunner {

  private final ThreadPool threadPool;

  private final Ae ae;

  @Override
  public void run(String... args) throws Exception {
    threadPool.execute(() -> {
      ae.run(args);
      System.exit(0);
    });
  }
}
