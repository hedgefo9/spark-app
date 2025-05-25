package com.hedgefo9.spark.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class Match {
    @JsonProperty("user_id1")
    Long userId1;
    @JsonProperty("user_id2")
    Long userId2;
    Long matchedAt;
}
