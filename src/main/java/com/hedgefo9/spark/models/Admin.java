package com.hedgefo9.spark.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Setter
@Getter
public class Admin implements Person {
    private int adminId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("email")
    private String email;
    @JsonProperty("passwordHash")
    private String passwordHash;
    @JsonProperty("createdAt")
    private Timestamp createdAt;
}
