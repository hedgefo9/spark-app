package com.hedgefo9.spark.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;


public record Like(@JsonProperty("sender_id") Long senderId,
                   @JsonProperty("receiver_id") Long receiverId,
                   @JsonProperty("timestamp") Long timestamp) {

}
