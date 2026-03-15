package com.njit.project1.common;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

public final class AwsClients {
    private AwsClients() {
    }

    public static S3Client createS3Client() {
        return S3Client.builder()
                .region(Config.getRegion())
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public static SqsClient createSqsClient() {
        return SqsClient.builder()
                .region(Config.getRegion())
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public static RekognitionClient createRekognitionClient() {
        return RekognitionClient.builder()
                .region(Config.getRegion())
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
