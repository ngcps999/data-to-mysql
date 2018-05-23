package com.mycompany.tahiti.analysis.utils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class Utility {

    public static String getResourcePath(String filePath) {
        //test absolute or relative path
        File file = new File(filePath);
        if (!file.isAbsolute()) {
            URL url = Utility.class.getClassLoader().getResource(filePath);
            if (url == null)
                throw new IllegalArgumentException(String.format("%s is not a valid format", filePath));
            file = new File(url.getFile());
        }
        if (!file.exists())
            throw new IllegalArgumentException(String.format("%s is not a valid file,%s", filePath, file.getPath()));

        return file.getPath();
    }

    public static List<String> getFileLines(String filePath) throws IOException {
        try(InputStream inputStream = Utility.class.getClassLoader().getResourceAsStream(filePath)) {
            try(InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                BufferedReader reader = new BufferedReader(streamReader);
                return reader.lines().collect(Collectors.toList());
            }
        }
    }
}
