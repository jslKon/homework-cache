package com.example.redisimplement.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class AirportDto {

    private long code;

    private String name;
}
