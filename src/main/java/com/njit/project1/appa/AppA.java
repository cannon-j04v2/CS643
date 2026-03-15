package com.njit.project1.appa;

import com.njit.project1.common.AwsClients;
import com.njit.project1.common.Config;
import com.njit.project1.common.RekognitionHelper;
import com.njit.project1.common.S3Helper;
import com.njit.project1.common.SqsHelper;
import java.util.List;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

public class AppA {
    public static void main(String[] args) {
        System.out.println("AppA started.");
        String bucketName = Config.getBucketName();
        String queueUrl = Config.getQueueUrl();

        try (S3Client s3Client = AwsClients.createS3Client();
             SqsClient sqsClient = AwsClients.createSqsClient();
             RekognitionClient rekognitionClient = AwsClients.createRekognitionClient()) {

            S3Helper s3Helper = new S3Helper(s3Client);
            SqsHelper sqsHelper = new SqsHelper(sqsClient, queueUrl);
            RekognitionHelper rekognitionHelper = new RekognitionHelper(rekognitionClient);

            List<String> imageKeys = Config.getImageKeys();
            for (String imageKey : imageKeys) {
                System.out.println("AppA processing image: " + imageKey);

                byte[] imageBytes = s3Helper.downloadObjectBytes(bucketName, imageKey);
                boolean carDetected = rekognitionHelper.detectCarInImage(imageBytes);

                if (carDetected) {
                    System.out.println("Car detected in " + imageKey + ". Sending to SQS.");
                    sqsHelper.sendImageKeyMessage(imageKey);
                } else {
                    System.out.println("No car detected in " + imageKey + ".");
                }
            }

            System.out.println("AppA finished all images. Sending termination message -1.");
            sqsHelper.sendTerminationMessage();
            System.out.println("AppA completed.");
        }
    }
}
