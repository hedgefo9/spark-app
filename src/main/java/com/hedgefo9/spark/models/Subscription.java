package com.hedgefo9.spark.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
public class Subscription {
    @JsonProperty("subscriptionId")
    private Long subscriptionId;
    @JsonProperty("userId")
    private Long userId;
    @JsonProperty("startAt")
    private Timestamp startAt;
    @JsonProperty("endAt")
    private Timestamp endAt;
    @JsonProperty("planType")
    private String planType;
}

