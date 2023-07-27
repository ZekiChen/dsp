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

  private final CheatingDataLoader cheatingDataLoader;

  @Override
  public void run(String... args) throws Exception {
    threadPool.execute(() -> {
      if (args == null || args.length <= 0) {
        System.exit(0);
      } else {
        switch (args[0]) {
          case TaskName.AE:
            ae.run(args);
            break;
          case TaskName.CHEATING_DATA_LOADER:
            cheatingDataLoader.run();
            break;
          default:
        }
      }
      System.exit(0);
    });
  }
}
