package com.jezhumble.javasysmon;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {

    private static final Pattern PROC_DIR_PATTERN = Pattern.compile("([\\d]*)");

    private final static FilenameFilter PROCESS_DIRECTORY_FILTER = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            File fileToTest = new File(dir, name);
            return fileToTest.isDirectory() && PROC_DIR_PATTERN.matcher(name).matches();
        }
    };

    public String[] pidsFromProcFilesystem() {
        return new File("/proc").list(FileUtils.PROCESS_DIRECTORY_FILTER);
    }

    public String slurp(String fileName) throws IOException {
        return slurpFromInputStream(new FileInputStream(fileName));
    }

    public byte[] slurpToByteArray(String fileName) throws IOException {
        File fileToRead = new File(fileName);
        byte[] contents = new byte[(int) fileToRead.length()];
        final InputStream inputStream = new FileInputStream(fileToRead);
        inputStream.read(contents);
        return contents;
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
