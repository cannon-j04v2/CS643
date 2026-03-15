package com.njit.project1.common;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.DetectTextRequest;
import software.amazon.awssdk.services.rekognition.model.DetectTextResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.Label;
import software.amazon.awssdk.services.rekognition.model.TextDetection;

public class RekognitionHelper {
    private static final float CONFIDENCE_THRESHOLD = 80.0f;

    private final RekognitionClient rekognitionClient;

    public RekognitionHelper(RekognitionClient rekognitionClient) {
        this.rekognitionClient = rekognitionClient;
    }

    public boolean detectCarInImage(byte[] imageBytes) {
        Image image = Image.builder()
                .bytes(SdkBytes.fromByteArray(imageBytes))
                .build();

        DetectLabelsRequest request = DetectLabelsRequest.builder()
                .image(image)
                .build();

        DetectLabelsResponse response = rekognitionClient.detectLabels(request);

        for (Label label : response.labels()) {
            if ("Car".equals(label.name()) && label.confidence() != null && label.confidence() > CONFIDENCE_THRESHOLD) {
                return true;
            }
        }

        return false;
    }

    public List<String> detectHighConfidenceText(byte[] imageBytes) {
        Image image = Image.builder()
                .bytes(SdkBytes.fromByteArray(imageBytes))
                .build();

        DetectTextRequest request = DetectTextRequest.builder()
                .image(image)
                .build();

        DetectTextResponse response = rekognitionClient.detectText(request);
        Set<String> uniqueText = new LinkedHashSet<>();

        for (TextDetection detection : response.textDetections()) {
            String detectedText = detection.detectedText();
            Float confidence = detection.confidence();

            if (detectedText == null || detectedText.isBlank()) {
                continue;
            }

            if (confidence != null && confidence > CONFIDENCE_THRESHOLD) {
                uniqueText.add(detectedText.trim());
            }
        }

        return new ArrayList<>(uniqueText);
    }
}
