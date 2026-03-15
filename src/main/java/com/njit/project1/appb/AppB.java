package com.njit.project1.appb;

import com.njit.project1.common.AwsClients;
import com.njit.project1.common.Config;
import com.njit.project1.common.FileHelper;
import com.njit.project1.common.RekognitionHelper;
import com.njit.project1.common.S3Helper;
import com.njit.project1.common.SqsHelper;
import java.util.List;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

public final class AppB {
    public static void main(String[] args) {
        System.out.println("AppB started.");
        String bucketName = Config.getBucketName();
        String queueUrl = Config.getQueueUrl();

        try (S3Client s3Client = AwsClients.createS3Client();
             SqsClient sqsClient = AwsClients.createSqsClient();
             RekognitionClient rekognitionClient = AwsClients.createRekognitionClient()) {

            S3Helper s3Helper = new S3Helper(s3Client);
            SqsHelper sqsHelper = new SqsHelper(sqsClient, queueUrl);
            RekognitionHelper rekognitionHelper = new RekognitionHelper(rekognitionClient);
            FileHelper fileHelper = new FileHelper("output.txt");

            boolean running = true;
            while (running) {
                List<Message> messages = sqsHelper.receiveMessages();

                if (messages == null || messages.isEmpty()) {
                    System.out.println("AppB: no messages received, polling again...");
                    continue;
                }

                for (Message message : messages) {
                    String body = message.body();
                    System.out.println("AppB received message: " + body);

                    if (SqsHelper.isTerminationMessage(body)) {
                        System.out.println("AppB received termination message. Shutting down.");
                        sqsHelper.deleteMessage(message.receiptHandle());
                        running = false;
                        break;
                    }

                    byte[] imageBytes = s3Helper.downloadObjectBytes(bucketName, body);
                    List<String> detectedText = rekognitionHelper.detectHighConfidenceText(imageBytes);

                    if (!detectedText.isEmpty()) {
                        String line = body + " : " + String.join(", ", detectedText);
                        fileHelper.appendLine(line);
                        System.out.println("AppB wrote output: " + line);
                    } else {
                        System.out.println("AppB found no high-confidence text for " + body);
                    }

                    sqsHelper.deleteMessage(message.receiptHandle());
                }
            }

            System.out.println("AppB exited cleanly.");
        }
    }
}
