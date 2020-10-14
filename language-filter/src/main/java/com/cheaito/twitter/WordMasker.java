package com.cheaito.twitter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class WordMasker {
    private static final List<String> badWordList = loadWorldList();

    private static List<String> loadWorldList() {
        try {
            Path cachedWordList = Paths.get(System.getProperty("java.io.tmpdir"), "profaneWordList.txt");
            if (Files.notExists(cachedWordList)) {
                try (InputStream inputStream = new URL("https://raw.githubusercontent.com/RobertJGabriel/Google-profanity-words/master/list.txt")
                        .openConnection().getInputStream()) {
                    Files.write(cachedWordList, inputStream.readAllBytes());
                }
            }
            return Files.readAllLines(cachedWordList);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load word list", e);
        }
    }

    public static String maskUndesirableWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] words = text.split("\\W+");
        String newText = text;

        for (String word : words) {
            if (badWordList.contains(word)) {
                newText = text.replace(word, "****");
            }
        }
        return newText;
    }
}
