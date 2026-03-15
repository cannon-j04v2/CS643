package com.njit.project1.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import software.amazon.awssdk.regions.Region;

public final class Config {
    private static final String BUCKET_NAME = "cs643-njit-project1";

    private Config() {
    }

    public static Region getRegion() {
        String regionValue = readRequiredEnv("AWS_REGION");
        return Region.of(regionValue);
    }

    public static String getQueueUrl() {
        return readRequiredEnv("SQS_QUEUE_URL");
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
        Optional<String> value = Optional.ofNullable(System.getenv(key)).map(String::trim);
        if (value.isEmpty() || value.get().isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value.get();
    }
}
