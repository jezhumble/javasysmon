package com.jezhumble.javasysmon;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {
    public String slurp(String fileName) throws IOException {
        return slurpFromInputStream(new FileInputStream(fileName));
    }

    public String slurpFromInputStream(InputStream stream) throws IOException {
        if (stream == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } finally {
            stream.close();
        }
        return sb.toString();
    }

    public String runRegexOnFile(Pattern pattern, String filename) {
        try {
            final String file = slurp(filename);
            Matcher matcher = pattern.matcher(file);
            matcher.find();
            final String firstMatch = matcher.group(1);
            if (firstMatch != null && firstMatch.length() > 0) {
                return firstMatch;
            }
        } catch (IOException e) {
            // return null to indicate failure
        }
        return null;
    }

}
