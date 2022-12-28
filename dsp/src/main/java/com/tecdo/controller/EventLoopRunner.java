package com.tecdo.controller;

import com.tecdo.common.ThreadPool;
import com.tecdo.constant.EventType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Automatically executed on startup after SpringApplication
 *
 * Created by Zeki on 2022/12/28
 **/
@Slf4j
@Component
@AllArgsConstructor
public class EventLoopRunner implements CommandLineRunner {

    private final MessageQueue messageQueue;

    @Override
    public void run(String... args) {
        ThreadPool.getInstance().execute(this::eventLoop);
    }

    private void eventLoop() {
        log.info("event loop start...");
        messageQueue.putMessage(EventType.SERVER_START);
        messageQueue.eventLoop();
    }
}
