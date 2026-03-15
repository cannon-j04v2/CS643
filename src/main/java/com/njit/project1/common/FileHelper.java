package com.njit.project1.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileHelper {
    private final Path outputPath;

    public FileHelper(String fileName) {
        this.outputPath = Paths.get(fileName);
    }

    public void appendLine(String line) {
        try {
            Files.writeString(
                    outputPath,
                    line + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to " + outputPath, e);
        }
    }
}
