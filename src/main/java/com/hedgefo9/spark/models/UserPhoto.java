package com.hedgefo9.spark.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class UserPhoto {
    @JsonProperty("photoId")
    private Long photoId;
    @JsonProperty("userId")
    private Long userId;
    @JsonProperty("fileName")
    private String fileName;
    @JsonProperty("uploadedAt")
    private Timestamp uploadedAt;
    @JsonProperty("isPrimary")
    private Boolean isPrimary;
}

