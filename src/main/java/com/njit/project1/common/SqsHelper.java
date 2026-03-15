package com.njit.project1.common;

import java.util.List;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class SqsHelper {
    private static final String MESSAGE_GROUP_ID = "project1";
    private static final String TERMINATION_MESSAGE = "-1";

    private final SqsClient sqsClient;
    private final String queueUrl;

    public SqsHelper(SqsClient sqsClient, String queueUrl) {
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
    }

    public void sendImageKeyMessage(String imageKey) {
        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(imageKey)
                .messageGroupId(MESSAGE_GROUP_ID)
                .build();
        sqsClient.sendMessage(request);
    }

    public void sendTerminationMessage() {
        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(TERMINATION_MESSAGE)
                .messageGroupId(MESSAGE_GROUP_ID)
                .build();
        sqsClient.sendMessage(request);
    }

    public List<Message> receiveMessages() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(5)
                .waitTimeSeconds(20)
                .build();

        ReceiveMessageResponse response = sqsClient.receiveMessage(request);
        return response.messages();
    }

    public void deleteMessage(String receiptHandle) {
        DeleteMessageRequest request = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(receiptHandle)
                .build();
        sqsClient.deleteMessage(request);
    }

    public static boolean isTerminationMessage(String messageBody) {
        return TERMINATION_MESSAGE.equals(messageBody);
    }
}
