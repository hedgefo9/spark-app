package com.hedgefo9.spark.models;

public record LikeEvent(Like like, LikeEventType type) {
    public enum LikeEventType {
        ADD, REMOVE
    }
}