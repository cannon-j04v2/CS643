package com.njit.project1.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Config {
    private static final String BUCKET_NAME = "cs643-njit-project1";

    private Config() {
    }

    public static String getQueueUrl() {
        return readRequiredEnv("SQS_QUEUE_URL");
    }

    public static Optional<String> getOptionalRegion() {
        return Optional.ofNullable(System.getenv("AWS_REGION"))
                .map(String::trim)
                .filter(value -> !value.isBlank());
    }

    public static String getBucketName() {
        return BUCKET_NAME;
    }

    public static List<String> getImageKeys() {
        List<String> imageKeys = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            imageKeys.add(i + ".jpg");
        }
        return imageKeys;
    }

    private static String readRequiredEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isBlank()) {
            throw new IllegalStateException(
                    "Missing required environment variable: " + key + ". Please export SQS_QUEUE_URL before running AppA/AppB."
            );
        }
        return value.trim();
    }
}
