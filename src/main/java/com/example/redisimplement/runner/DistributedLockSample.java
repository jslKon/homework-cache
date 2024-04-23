package com.example.redisimplement.runner;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class DistributedLockSample implements ApplicationRunner {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        //user 1 booking
        new Thread(
                () -> {
                    try {
                        bookSeat();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                },
                "transaction1"
        ).start();

        Thread.sleep(1000);

        //user 2 booking failed, log error
        new Thread(
                () -> {
                    try {
                        bookSeat();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                },
                "transaction2"
        ).start();

        Thread.sleep(10000);

        //user 2 booking again, not log error
        new Thread(
                () -> {
                    try {
                        bookSeat();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                },
                "transaction2"
        ).start();
    }

    public void bookSeat() throws InterruptedException {
        //check if other process is booking
        if (lockCurrentSeat("FA634", "A34_S012C")) {
            log.error("Someone is booking this seat, try again later or choose another seat");
            return;
        }

        log.info("Your seat is currently available");
        //do booking
        Thread.sleep(10000);

        //not book anymore
        releaseLockCurrentSeat("FA634", "A34_S012C");
    }

    private boolean lockCurrentSeat(String flightId, String seatId) {
        return Boolean.FALSE.equals(
                redisTemplate.opsForValue()
                        .setIfAbsent(flightId + seatId, 1, 1, TimeUnit.MINUTES)
        );
    }

    private void releaseLockCurrentSeat(String flightId, String seatId) {
        log.info("Abort booking");
        redisTemplate.delete(flightId + seatId);
    }
}
