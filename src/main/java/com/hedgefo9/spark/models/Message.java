package com.hedgefo9.spark.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;


public record Message(Long messageId,
                      @JsonProperty("senderId")
                      Long senderId,
                      @JsonProperty("receiverId")
                      Long receiverId,
                      @JsonProperty("content")
                      String content,
                      @JsonProperty("sentAt")
                      Timestamp sentAt,
                      Boolean isRead) {
}
