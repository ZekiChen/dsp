package com.tecdo.fsm.task;

import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

@Component
public class TaskPool {

    private final Queue<Task> pool = new LinkedList<>();

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
