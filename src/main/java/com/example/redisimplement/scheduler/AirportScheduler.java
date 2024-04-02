package com.example.redisimplement.scheduler;

import com.example.redisimplement.dto.AirportDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/api/v1/airport")
@RequiredArgsConstructor
public class AirportScheduler {

    public static final int MIN_TTL = 10;
    public static final int MAX_TTL = 100;

    private final RedisTemplate<String, Object> redisTemplate;

    private Map<Long, AirportDto> airportDtoMap;

    @PostConstruct
    void setup() {
        airportDtoMap = Stream.of(
                AirportDto.builder().code(1).name("Airport1").build(),
                AirportDto.builder().code(2).name("Airport2").build(),
                AirportDto.builder().code(3).name("Airport3").build(),
                AirportDto.builder().code(4).name("Airport4").build(),
                AirportDto.builder().code(5).name("Airport5").build()
        ).collect(Collectors.toMap(AirportDto::getCode, Function.identity()));
    }


    @Scheduled(fixedRate = 2, timeUnit = TimeUnit.SECONDS)
    public void getAirPort() {
        long id = new Random().nextLong(1, 5);
        log.info("Id: {}", id);
        String key = String.valueOf(id);
        long ttl = new Random().nextLong(MIN_TTL, MAX_TTL);

        //check key exist in cache
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            Object airport = redisTemplate.opsForValue().get(key);
            log.info("Airport info in cache expires in {} : {}", redisTemplate.getExpire(key), airport);
        }
        //key not exist then get object from db
        else {
            AirportDto airportDto = airportDtoMap.get(id);
            log.info("Airport info in DB: {}", airportDto);

            //put object to redis
            redisTemplate.opsForValue().setIfAbsent(key, airportDto.toString(), ttl, TimeUnit.SECONDS);
            log.info("Key put to redis: {}", id);
        }
    }
}
