package com.njit.project1.common;

import java.util.Optional;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.RekognitionClientBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;

public final class AwsClients {
    private AwsClients() {
    }

    public static S3Client createS3Client() {
        S3ClientBuilder builder = S3Client.builder();
        applyOptionalRegion(builder);
        return builder.build();
    }

    public static SqsClient createSqsClient() {
        SqsClientBuilder builder = SqsClient.builder();
        applyOptionalRegion(builder);
        return builder.build();
    }

    public static RekognitionClient createRekognitionClient() {
        RekognitionClientBuilder builder = RekognitionClient.builder();
        applyOptionalRegion(builder);
        return builder.build();
    }

    private static void applyOptionalRegion(AwsClientBuilder<?, ?> builder) {
        Optional<String> optionalRegion = Config.getOptionalRegion();
        if (optionalRegion.isPresent()) {
            builder.region(Region.of(optionalRegion.get()));
        }
    }
}
