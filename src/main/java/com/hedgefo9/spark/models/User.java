package com.hedgefo9.spark.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@ToString
public class User implements Person {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("gender")
    private Integer gender;

    @JsonProperty("birth_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthDate;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("email")
    private String email;

    @JsonProperty("city")
    private String city;

    @JsonProperty("education")
    private Integer education;

    @JsonProperty("smokes")
    private Boolean smokes;

    @JsonProperty("password_hash")
    private String passwordHash;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
