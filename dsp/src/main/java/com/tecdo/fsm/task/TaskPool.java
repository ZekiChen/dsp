package com.tecdo.fsm.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;

public class TaskPool {
  private static final Logger logger = LoggerFactory.getLogger(TaskPool.class);
  private static final TaskPool instance = new TaskPool();

  private Queue<Task> pool = new LinkedList<>();

  public static TaskPool getInstance() {
    return instance;
  }

  private TaskPool() {
  }

  public Task get() {
    Task task = pool.poll();
    if (task == null) {
      task = new Task();
    }
    return task;
  }

  public void release(Task task) {
    if (task != null) {
      task.reset();
      pool.offer(task);
    }
  }

}
