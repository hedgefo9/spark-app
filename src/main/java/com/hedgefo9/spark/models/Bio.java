package com.hedgefo9.spark.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class Bio {
    @JsonProperty("userId")
    private Long userId;
    @JsonProperty("aboutMe")
    private String aboutMe;
    @JsonProperty("lookingFor")
    private String lookingFor;
    private LocalDateTime updatedAt;
}
