package com.github.evgeniymelnikov.piro.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LoadFileUtils {

    public static String readFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get("src/test/resources/" + filePath)), Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}