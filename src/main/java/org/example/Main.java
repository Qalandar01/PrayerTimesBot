package org.example;

import java.time.LocalDateTime;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        BotController.start();
        long initialDelay = computeInitialDelay();

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
        executorService.scheduleAtFixedRate(BotService::sendAllUsersPrayerTimes, initialDelay,24,TimeUnit.HOURS);
    }
    private static long computeInitialDelay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(9).withMinute(0).withSecond(0);

        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }

        return java.time.Duration.between(now, nextRun).getSeconds();
    }
}